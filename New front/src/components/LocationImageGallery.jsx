import React, { useState, useRef } from 'react';
import { Camera, Plus, X, Upload, Image as ImageIcon } from 'lucide-react';
import GlassCard from './GlassCard';
import PrimaryButton from './PrimaryButton';

const LocationImageGallery = ({ 
  images = [], 
  onImagesChange, 
  maxImages = 10, 
  isEditing = false 
}) => {
  const [uploadError, setUploadError] = useState('');
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  const validateFiles = (files) => {
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    const maxSize = 5 * 1024 * 1024; // 5MB
    
    for (let file of files) {
      if (!validTypes.includes(file.type)) {
        return 'Only JPEG, PNG, and WebP images are allowed';
      }
      if (file.size > maxSize) {
        return 'Each image must be less than 5MB';
      }
    }
    
    if (images.length + files.length > maxImages) {
      return `Maximum ${maxImages} images allowed`;
    }
    
    return null;
  };

  const handleFileSelect = async (e) => {
    const files = Array.from(e.target.files);
    if (!files.length) return;

    const error = validateFiles(files);
    if (error) {
      setUploadError(error);
      return;
    }

    setUploadError('');
    setUploading(true);

    try {
      // Create preview URLs for immediate display
      const newImagePreviews = await Promise.all(
        files.map(file => {
          return new Promise((resolve) => {
            const reader = new FileReader();
            reader.onload = (e) => resolve({
              url: e.target.result,
              file: file,
              isNew: true,
              id: Date.now() + Math.random()
            });
            reader.readAsDataURL(file);
          });
        })
      );

      // Update images array
      const updatedImages = [...images, ...newImagePreviews];
      onImagesChange(updatedImages);
      
    } catch (error) {
      setUploadError('Failed to process images');
    } finally {
      setUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const removeImage = (index) => {
    const updatedImages = images.filter((_, i) => i !== index);
    onImagesChange(updatedImages);
  };

  const openFileDialog = () => {
    fileInputRef.current?.click();
  };

  const remainingSlots = maxImages - images.length;

  return (
    <div className="space-y-4">
      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept="image/jpeg,image/jpg,image/png,image/webp"
        onChange={handleFileSelect}
        className="hidden"
      />

      {/* Images Display */}
      {images.length === 0 ? (
        // Empty state - show clickable placeholder
        <div 
          onClick={openFileDialog}
          className="cursor-pointer group"
        >
          <div className="border-2 border-dashed border-white/30 rounded-xl p-8 text-center hover:border-white/50 hover:bg-white/5 transition-all duration-300">
            <div className="flex flex-col items-center space-y-3">
              <div className="p-4 bg-white/10 rounded-full group-hover:bg-white/20 transition-colors">
                <ImageIcon className="w-8 h-8 text-white" />
              </div>
              <div>
                <p className="text-white font-medium">Add Location Images</p>
                <p className="text-white/70 text-sm mt-1">
                  Click to upload up to {maxImages} images
                </p>
                <p className="text-white/50 text-xs mt-1">
                  JPEG, PNG, WebP • Max 5MB each
                </p>
              </div>
            </div>
          </div>
        </div>
      ) : (
        // Image gallery
        <div className="space-y-4">
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {images.map((image, index) => (
              <div key={image.id || index} className="relative group">
                <div className="aspect-square rounded-lg overflow-hidden bg-white/10">
                  <img
                    src={image.url || image}
                    alt={`Location image ${index + 1}`}
                    className="w-full h-full object-cover"
                  />
                  {uploading && image.isNew && (
                    <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                      <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-white"></div>
                    </div>
                  )}
                </div>
                
                {/* Remove button */}
                {(isEditing || image.isNew) && (
                  <button
                    type="button"
                    onClick={() => removeImage(index)}
                    className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 hover:bg-red-600 text-white rounded-full flex items-center justify-center text-sm opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                  >
                    <X className="w-3 h-3" />
                  </button>
                )}
                
                {/* Main image indicator */}
                {index === 0 && (
                  <div className="absolute bottom-2 left-2 bg-violet-500 text-white text-xs px-2 py-1 rounded">
                    Main
                  </div>
                )}
                
                {/* New image indicator */}
                {image.isNew && (
                  <div className="absolute top-2 left-2 bg-green-500 text-white text-xs px-2 py-1 rounded">
                    New
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Add more images button */}
          {remainingSlots > 0 && (
            <button
              type="button"
              onClick={openFileDialog}
              disabled={uploading}
              className="w-full border-2 border-dashed border-white/30 rounded-xl p-4 text-center hover:border-white/50 hover:bg-white/5 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300"
            >
              <div className="flex flex-col items-center space-y-2">
                <div className="p-2 bg-white/10 rounded-full">
                  <Plus className="w-5 h-5 text-white" />
                </div>
                <div>
                  <p className="text-white font-medium">Add More Images</p>
                  <p className="text-white/70 text-sm">
                    {remainingSlots} slot{remainingSlots !== 1 ? 's' : ''} remaining
                  </p>
                </div>
              </div>
            </button>
          )}
        </div>
      )}

      {/* Upload error */}
      {uploadError && (
        <div className="bg-red-500/20 border border-red-500/30 rounded-lg p-3">
          <p className="text-red-200 text-sm">{uploadError}</p>
        </div>
      )}

      {/* Upload progress */}
      {uploading && (
        <div className="bg-blue-500/20 border border-blue-500/30 rounded-lg p-3">
          <p className="text-blue-200 text-sm flex items-center gap-2">
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-200"></div>
            Processing images...
          </p>
        </div>
      )}

      {/* Info */}
      <div className="bg-white/5 rounded-lg p-3">
        <p className="text-white/70 text-xs">
          💡 <strong>Tips:</strong> Upload high-quality images that showcase your location. 
          The first image will be used as the main preview. You can upload up to {maxImages} images.
        </p>
      </div>
    </div>
  );
};

export default LocationImageGallery;
