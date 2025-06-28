import React, { useState } from 'react';
import { Upload, X, Image as ImageIcon, Loader2 } from 'lucide-react';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const ImageUploadComponent = ({ unitId, currentImages = [], onImagesUpdated }) => {
  const { success, error: showError } = useToast();
  const [uploading, setUploading] = useState(false);
  const [dragActive, setDragActive] = useState(false);

  const handleImageUpload = async (files) => {
    if (!files || files.length === 0) return;

    setUploading(true);
    try {
      const formData = new FormData();
      Array.from(files).forEach(file => {
        formData.append('files', file);
      });

      const response = await api.post(`/api/uploads/unit/${unitId}/images`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      success('Images Uploaded', `Successfully uploaded ${files.length} image(s)`);
      onImagesUpdated(response.data.unit.images);
    } catch (err) {
      showError('Upload Failed', err.response?.data?.error || 'Failed to upload images');
    } finally {
      setUploading(false);
    }
  };

  const handleImageDelete = async (imageUrl) => {
    try {
      await api.delete(`/api/uploads/unit/${unitId}/images`, {
        params: { imageUrl }
      });

      success('Image Deleted', 'Image removed successfully');
      onImagesUpdated(currentImages.filter(img => img !== imageUrl));
    } catch (err) {
      showError('Delete Failed', err.response?.data?.error || 'Failed to delete image');
    }
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    const files = e.dataTransfer.files;
    handleImageUpload(files);
  };

  const handleFileInput = (e) => {
    handleImageUpload(e.target.files);
  };

  return (
    <div className="space-y-6">
      {/* Upload Area */}
      <div
        className={`relative border-2 border-dashed rounded-lg p-8 text-center transition-colors duration-300 ${
          dragActive 
            ? 'border-violet-400 bg-violet-500/10' 
            : 'border-white/20 hover:border-violet-400/50'
        }`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
      >
        <input
          type="file"
          multiple
          accept="image/*"
          onChange={handleFileInput}
          className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
          disabled={uploading}
        />
        
        <div className="space-y-4">
          {uploading ? (
            <Loader2 className="w-12 h-12 mx-auto text-violet-400 animate-spin" />
          ) : (
            <Upload className="w-12 h-12 mx-auto text-violet-400" />
          )}
          
          <div>
            <p className="text-lg font-medium text-white">
              {uploading ? 'Uploading images...' : 'Upload Property Images'}
            </p>
            <p className="text-white/70 mt-2">
              Drag and drop images here, or click to select files
            </p>
            <p className="text-white/50 text-sm mt-1">
              Supports: JPG, PNG, WebP (Max 10MB each)
            </p>
          </div>
        </div>
      </div>

      {/* Current Images */}
      {currentImages && currentImages.length > 0 && (
        <div>
          <h4 className="text-lg font-medium text-white mb-4 flex items-center gap-2">
            <ImageIcon className="w-5 h-5" />
            Current Images ({currentImages.length})
          </h4>
          
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {currentImages.map((imageUrl, index) => (
              <div key={index} className="relative group">
                <div className="aspect-square rounded-lg overflow-hidden bg-white/10 backdrop-blur-xl">
                  <img
                    src={imageUrl.startsWith('http') ? imageUrl : `http://localhost:8080${imageUrl}`}
                    alt={`Property image ${index + 1}`}
                    className="w-full h-full object-cover"
                  />
                </div>
                
                <button
                  onClick={() => handleImageDelete(imageUrl)}
                  className="absolute top-2 right-2 bg-red-500 hover:bg-red-600 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default ImageUploadComponent;
