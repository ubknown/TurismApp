package com.licentarazu.turismapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licentarazu.turismapp.model.AccommodationPhoto;
import com.licentarazu.turismapp.repository.AccommodationPhotoRepository;

@Service
@Transactional
public class AccommodationPhotoService {
    
    @Autowired
    private AccommodationPhotoRepository photoRepository;
    
    public List<AccommodationPhoto> getPhotosByUnitId(Long unitId) {
        return photoRepository.findByAccommodationUnitId(unitId);
    }
    
    public AccommodationPhoto savePhoto(AccommodationPhoto photo) {
        return photoRepository.save(photo);
    }
    
    public void savePhotos(List<AccommodationPhoto> photos) {
        photoRepository.saveAll(photos);
    }
    
    public void deletePhotosByUnitId(Long unitId) {
        photoRepository.deleteByAccommodationUnitId(unitId);
    }
    
    public void deletePhoto(Long photoId) {
        photoRepository.deleteById(photoId);
    }
    
    public long countPhotosByUnitId(Long unitId) {
        return photoRepository.countByAccommodationUnitId(unitId);
    }
}
