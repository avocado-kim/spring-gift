package gift.wish;

import gift.global.ForbiddenException;
import gift.product.Product;
import gift.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class WishService {
    private final WishRepository wishRepository;
    private final ProductRepository productRepository;

    public WishService(WishRepository wishRepository, ProductRepository productRepository) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
    }

    public Page<WishResponse> getWishes(Long memberId, Pageable pageable) {
        return wishRepository.findByMemberId(memberId, pageable).map(WishResponse::from);
    }

    public WishAddResult addWish(Long memberId, Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("Product not found."));
        return wishRepository.findByMemberIdAndProductId(memberId, productId)
            .map(existing -> new WishAddResult(WishResponse.from(existing), false))
            .orElseGet(() -> {
                Wish saved = wishRepository.save(new Wish(memberId, product));
                return new WishAddResult(WishResponse.from(saved), true);
            });
    }

    public void removeWish(Long memberId, Long wishId) {
        Wish wish = wishRepository.findById(wishId)
            .orElseThrow(() -> new NoSuchElementException("Wish not found."));
        if (!wish.getMemberId().equals(memberId)) {
            throw new ForbiddenException();
        }
        wishRepository.delete(wish);
    }

    public record WishAddResult(WishResponse wish, boolean isNew) {
    }
}