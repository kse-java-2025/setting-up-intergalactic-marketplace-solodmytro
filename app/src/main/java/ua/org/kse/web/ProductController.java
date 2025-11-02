package ua.org.kse.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.org.kse.dto.ProductCreateDto;
import ua.org.kse.dto.ProductDto;
import ua.org.kse.dto.ProductListDto;
import ua.org.kse.dto.ProductUpdateDto;
import ua.org.kse.service.ProductService;

@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ProductListDto> getAll(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ResponseEntity.ok(service.getAll(page, size));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductCreateDto dto) {
        var created = service.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable String id, @Valid @RequestBody ProductUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}