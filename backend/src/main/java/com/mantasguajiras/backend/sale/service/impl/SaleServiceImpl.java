package com.mantasguajiras.backend.sale.service.impl;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mantasguajiras.backend.sale.dto.requests.SaleRequest;
import com.mantasguajiras.backend.sale.dto.response.SaleResponse;
import com.mantasguajiras.backend.sale.entity.Sale;
import com.mantasguajiras.backend.sale.mapper.SaleMapper;
import com.mantasguajiras.backend.sale.repository.SaleRepository;
import com.mantasguajiras.backend.sale.service.SaleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleMapper saleMapper;

    @Override
    public SaleResponse create(SaleRequest saleRequest) {
        Sale sale = saleMapper.toEntity(saleRequest);
        Sale savedSale = saleRepository.save(sale);
        return saleMapper.toResponse(savedSale);
    }

    @Override
    public SaleResponse update(UUID id, SaleRequest saleRequest) {
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
        saleMapper.updateEntity(saleRequest, existingSale);
        Sale updatedSale = saleRepository.save(existingSale);
        return saleMapper.toResponse(updatedSale);
    }

    @Override
    public SaleResponse findById(UUID id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found with id: " + id));
        return saleMapper.toResponse(sale);
    }

    @Override
    public List<SaleResponse> findAll() {
        return saleRepository.findAll().stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        saleRepository.deleteById(id);
    }
}
