package gift.wish;

import gift.global.ForbiddenException;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.product.Product;
import gift.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Service
public class WishService {
    private final WishRepository wishRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public WishService(
        WishRepository wishRepository,
        ProductRepository productRepository,
        MemberRepository memberRepository
    ) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }

    public Page<WishResponse> getWishes(Long memberId, Pageable pageable) {
        return wishRepository.findByMember_Id(memberId, pageable).map(WishResponse::from);
    }

    public WishResponse addWish(Long memberId, Long productId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Member not found."));
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("Product not found."));
        if (wishRepository.findByMember_IdAndProductId(memberId, productId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 추가된 상품입니다.");
        }
        return WishResponse.from(wishRepository.save(new Wish(member, product)));
    }

    public void removeWish(Long memberId, Long wishId) {
        Wish wish = wishRepository.findById(wishId)
            .orElseThrow(() -> new NoSuchElementException("Wish not found."));
        if (!wish.getMemberId().equals(memberId)) {
            throw new ForbiddenException();
        }
        wishRepository.delete(wish);
    }
}