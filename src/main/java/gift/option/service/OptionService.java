package gift.option.service;
import gift.option.domain.Option;
import gift.option.domain.OptionNameValidator;
import gift.option.dto.OptionRequest;
import gift.option.dto.OptionResponse;
import gift.option.repository.OptionRepository;

import gift.product.domain.Product;
import gift.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OptionService {
    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public OptionService(OptionRepository optionRepository, ProductRepository productRepository) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
    }

    public List<OptionResponse> getOptions(Long productId) {
        productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        return optionRepository.findByProductId(productId).stream().map(OptionResponse::from).toList();
    }

    public OptionResponse createOption(Long productId, OptionRequest request) {
        validateName(request.name());
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        if (optionRepository.existsByProductIdAndName(productId, request.name())) {
            throw new IllegalArgumentException("이미 존재하는 옵션명입니다.");
        }
        return OptionResponse.from(optionRepository.save(new Option(product, request.name(), request.quantity())));
    }

    public void deleteOption(Long productId, Long optionId) {
        productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        if (optionRepository.findByProductId(productId).size() <= 1) {
            throw new IllegalArgumentException("옵션이 1개인 상품은 옵션을 삭제할 수 없습니다.");
        }
        Option option = optionRepository.findById(optionId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 옵션입니다."));
        if (!option.getProduct().getId().equals(productId)) {
            throw new NoSuchElementException("존재하지 않는 옵션입니다.");
        }
        optionRepository.delete(option);
    }

    private void validateName(String name) {
        var errors = OptionNameValidator.validate(name);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }
}