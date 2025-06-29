import React, { useState, useEffect } from 'react';
import { ownerApplicationService } from '../services/ownerApplicationService';
import api from '../services/axios';
import GlassCard from './GlassCard';
import PrimaryButton from './PrimaryButton';
import LoadingSpinner from './LoadingSpinner';
import AlertBox from './AlertBox';

const AdminOwnerApplicationsPanel = () => {
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [copiedLink, setCopiedLink] = useState(null);

    useEffect(() => {
        fetchPendingApplications();
    }, []);

    const fetchPendingApplications = async () => {
        try {
            setLoading(true);
            const response = await api.get('/api/owner-applications/pending');
            setApplications(response.data);
        } catch (err) {
            console.error('Error fetching pending applications:', err);
            setError('Nu s-au putut încărca cererile în așteptare.');
        } finally {
            setLoading(false);
        }
    };

    const handleCopyLink = async (link, linkType, applicationId) => {
        try {
            const success = await ownerApplicationService.copyToClipboard(link);
            if (success) {
                setCopiedLink(`${linkType}-${applicationId}`);
                setTimeout(() => setCopiedLink(null), 2000); // Clear after 2 seconds
            } else {
                alert('Nu s-a putut copia link-ul. Te rugăm să îl copiezi manual.');
            }
        } catch (err) {
            console.error('Error copying link:', err);
            alert('Eroare la copierea link-ului.');
        }
    };

    const generateEmailContent = (application) => {
        return ownerApplicationService.generateAdminNotificationEmailContent(application);
    };

    const sendNotificationEmail = async (application) => {
        try {
            const emailContent = generateEmailContent(application);
            // This would typically call your email service
            // For now, we'll just show the content in a modal or copy it
            const subject = `📋 Cerere Nouă de Proprietar - ${application.user.firstName} ${application.user.lastName}`;
            
            // Copy email content to clipboard for manual sending
            const fullEmailContent = `Subject: ${subject}\n\n${emailContent}`;
            const success = await ownerApplicationService.copyToClipboard(fullEmailContent);
            
            if (success) {
                alert('Conținutul emailului a fost copiat în clipboard. Poți să îl trimiți manual.');
            } else {
                // Fallback: show content in a new window
                const newWindow = window.open();
                newWindow.document.write(`
                    <html>
                        <head><title>Email Content</title></head>
                        <body>
                            <h2>Subject: ${subject}</h2>
                            <div>${emailContent}</div>
                        </body>
                    </html>
                `);
            }
        } catch (err) {
            console.error('Error generating email content:', err);
            alert('Eroare la generarea conținutului emailului.');
        }
    };

    if (loading) {
        return (
            <GlassCard>
                <div className="flex items-center justify-center p-8">
                    <LoadingSpinner size="large" />
                    <span className="ml-3 text-white">Se încarcă cererile...</span>
                </div>
            </GlassCard>
        );
    }

    if (error) {
        return (
            <GlassCard>
                <AlertBox type="error" message={error} />
                <PrimaryButton 
                    onClick={fetchPendingApplications}
                    className="mt-4"
                >
                    Încearcă din nou
                </PrimaryButton>
            </GlassCard>
        );
    }

    return (
        <div className="space-y-6">
            <GlassCard>
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-2xl font-bold text-white">
                        📋 Cereri în Așteptare ({applications.length})
                    </h2>
                    <PrimaryButton 
                        onClick={fetchPendingApplications}
                        className="text-sm"
                    >
                        🔄 Actualizează
                    </PrimaryButton>
                </div>

                {applications.length === 0 ? (
                    <div className="text-center py-8">
                        <div className="text-6xl mb-4">✅</div>
                        <p className="text-xl text-white font-semibold">Nu sunt cereri în așteptare</p>
                        <p className="text-blue-200 mt-2">Toate cererile au fost procesate.</p>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {applications.map((application) => {
                            const approvalLink = ownerApplicationService.generateApprovalLink(
                                application.id, 
                                application.submittedAt
                            );
                            const rejectionLink = ownerApplicationService.generateRejectionLink(
                                application.id, 
                                application.submittedAt
                            );

                            return (
                                <div 
                                    key={application.id} 
                                    className="bg-white/10 rounded-lg p-6 border border-white/20"
                                >
                                    <div className="grid md:grid-cols-2 gap-6">
                                        {/* Application Details */}
                                        <div>
                                            <h3 className="text-lg font-semibold text-white mb-3">
                                                👤 {application.user.firstName} {application.user.lastName}
                                            </h3>
                                            <div className="space-y-2 text-blue-100">
                                                <p><strong>Email:</strong> {application.user.email}</p>
                                                <p><strong>Data cererii:</strong> {new Date(application.submittedAt).toLocaleDateString('ro-RO')}</p>
                                                <p><strong>Status:</strong> 
                                                    <span className="bg-yellow-500/20 text-yellow-200 px-2 py-1 rounded ml-2">
                                                        {application.status}
                                                    </span>
                                                </p>
                                                {application.message && (
                                                    <div className="mt-3">
                                                        <p><strong>Mesaj:</strong></p>
                                                        <div className="bg-white/5 rounded p-3 mt-1">
                                                            <p className="text-sm">{application.message}</p>
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        </div>

                                        {/* Admin Actions */}
                                        <div>
                                            <h4 className="text-lg font-semibold text-white mb-3">🔧 Acțiuni Administrative</h4>
                                            
                                            {/* Quick Action Buttons */}
                                            <div className="space-y-3 mb-4">
                                                <div className="flex space-x-2">
                                                    <a
                                                        href={approvalLink}
                                                        target="_blank"
                                                        rel="noopener noreferrer"
                                                        className="flex-1 bg-green-600 hover:bg-green-700 text-white py-2 px-3 rounded text-center text-sm font-medium transition-colors"
                                                    >
                                                        ✅ Aprobare
                                                    </a>
                                                    <a
                                                        href={rejectionLink}
                                                        target="_blank"
                                                        rel="noopener noreferrer"
                                                        className="flex-1 bg-red-600 hover:bg-red-700 text-white py-2 px-3 rounded text-center text-sm font-medium transition-colors"
                                                    >
                                                        ❌ Respingere
                                                    </a>
                                                </div>
                                            </div>

                                            {/* Copy Links */}
                                            <div className="space-y-2">
                                                <div className="flex items-center space-x-2">
                                                    <button
                                                        onClick={() => handleCopyLink(approvalLink, 'approve', application.id)}
                                                        className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-1 px-2 rounded text-xs transition-colors"
                                                    >
                                                        {copiedLink === `approve-${application.id}` ? '✓ Copiat!' : '📋 Copiază link aprobare'}
                                                    </button>
                                                </div>
                                                <div className="flex items-center space-x-2">
                                                    <button
                                                        onClick={() => handleCopyLink(rejectionLink, 'reject', application.id)}
                                                        className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-1 px-2 rounded text-xs transition-colors"
                                                    >
                                                        {copiedLink === `reject-${application.id}` ? '✓ Copiat!' : '📋 Copiază link respingere'}
                                                    </button>
                                                </div>
                                            </div>

                                            {/* Email Template */}
                                            <div className="mt-4">
                                                <button
                                                    onClick={() => sendNotificationEmail(application)}
                                                    className="w-full bg-purple-600 hover:bg-purple-700 text-white py-2 px-3 rounded text-sm font-medium transition-colors"
                                                >
                                                    📧 Generează Email Admin
                                                </button>
                                            </div>

                                            {/* Security Info */}
                                            <div className="mt-4 bg-white/5 rounded p-3">
                                                <p className="text-xs text-blue-200">
                                                    🔒 Link-urile conțin token-uri de securitate și vor solicita parola administratorului.
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </GlassCard>

            {/* Instructions */}
            <GlassCard>
                <h3 className="text-lg font-semibold text-white mb-3">📚 Instrucțiuni de utilizare</h3>
                <div className="space-y-2 text-blue-100 text-sm">
                    <p><strong>1.</strong> Fă clic pe butoanele "✅ Aprobare" sau "❌ Respingere" pentru a accesa pagina de confirmare.</p>
                    <p><strong>2.</strong> Vei fi solicitat să introduci parola administratorului (<code className="bg-white/10 px-1 rounded">Rzvtare112</code>).</p>
                    <p><strong>3.</strong> După confirmare, solicitantul va primi automat un email cu decizia.</p>
                    <p><strong>4.</strong> Poți copia link-urile pentru a le trimite prin email altor administratori.</p>
                    <p><strong>5.</strong> Fiecare cerere poate fi procesată o singură dată - nu se poate anula.</p>
                </div>
            </GlassCard>
        </div>
    );
};

export default AdminOwnerApplicationsPanel;
