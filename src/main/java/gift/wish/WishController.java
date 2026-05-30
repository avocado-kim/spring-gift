package gift.wish;

import gift.member.Member;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/wishes")
public class WishController {
    private final WishService wishService;

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @GetMapping
    public ResponseEntity<Page<WishResponse>> getWishes(Member member, Pageable pageable) {
        return ResponseEntity.ok(wishService.getWishes(member.getId(), pageable));
    }

    @PostMapping
    public ResponseEntity<WishResponse> addWish(Member member, @Valid @RequestBody WishRequest request) {
        var result = wishService.addWish(member.getId(), request.productId());
        if (result.isNew()) {
            return ResponseEntity.created(URI.create("/api/wishes/" + result.wish().id()))
                .body(result.wish());
        }
        return ResponseEntity.ok(result.wish());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeWish(Member member, @PathVariable Long id) {
        wishService.removeWish(member.getId(), id);
        return ResponseEntity.noContent().build();
    }
}