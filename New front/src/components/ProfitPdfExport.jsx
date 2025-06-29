import React, { useState } from 'react';
import { Download, FileText, Calendar } from 'lucide-react';
import PrimaryButton from './PrimaryButton';
import { useToast } from '../context/ToastContext';
import api from '../services/axios';

const ProfitPdfExport = ({ className = "" }) => {
  const [isGenerating, setIsGenerating] = useState(false);
  const [months, setMonths] = useState(12);
  const { success, error: showError } = useToast();

  const handleDownloadPdf = async () => {
    try {
      setIsGenerating(true);
      
      const response = await api.get(`/api/units/my-units/profit/export-pdf?months=${months}`, {
        responseType: 'blob'
      });

      // Create blob link to download
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const downloadUrl = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.download = `profit-report-${months}months-${new Date().toISOString().split('T')[0]}.pdf`;
      
      // Trigger download
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(downloadUrl);

      success('PDF Downloaded', `Profit report for last ${months} months has been downloaded successfully.`);
      
    } catch (error) {
      console.error('Error downloading PDF:', error);
      showError('Download Failed', error.response?.data?.message || 'Failed to generate PDF report. Please try again.');
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div className={`flex flex-col sm:flex-row items-center gap-4 ${className}`}>
      {/* Time Period Selector */}
      <div className="flex items-center gap-2">
        <Calendar className="w-4 h-4 text-violet-300" />
        <label className="text-sm text-violet-200">Report Period:</label>
        <select
          value={months}
          onChange={(e) => setMonths(Number(e.target.value))}
          className="px-3 py-2 bg-white/10 border border-white/20 rounded-lg text-white text-sm focus:outline-none focus:ring-2 focus:ring-violet-500/50"
          disabled={isGenerating}
        >
          <option value={1} className="bg-slate-800">Last 1 Month</option>
          <option value={3} className="bg-slate-800">Last 3 Months</option>
          <option value={6} className="bg-slate-800">Last 6 Months</option>
          <option value={12} className="bg-slate-800">Last 12 Months</option>
          <option value={24} className="bg-slate-800">Last 24 Months</option>
          <option value={0} className="bg-slate-800">All Time</option>
        </select>
      </div>

      {/* Download Button */}
      <PrimaryButton
        onClick={handleDownloadPdf}
        disabled={isGenerating}
        className="flex items-center gap-2 px-6 py-2"
        variant="secondary"
      >
        {isGenerating ? (
          <>
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
            Generating PDF...
          </>
        ) : (
          <>
            <Download className="w-4 h-4" />
            <FileText className="w-4 h-4" />
            Download PDF Report
          </>
        )}
      </PrimaryButton>
    </div>
  );
};

export default ProfitPdfExport;
