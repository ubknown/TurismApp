import React, { useState } from 'react';
import { ownerApplicationService } from '../services/ownerApplicationService';
import BackgroundLayer from '../components/BackgroundLayer';
import GlassCard from '../components/GlassCard';
import PrimaryButton from '../components/PrimaryButton';
import AdminOwnerApplicationsPanel from '../components/AdminOwnerApplicationsPanel';

const AdminDemoPage = () => {
    const [activeTab, setActiveTab] = useState('overview');

    const sampleApplication = {
        id: 1,
        submittedAt: new Date().toISOString(),
        user: {
            firstName: 'John',
            lastName: 'Doe',
            email: 'john.doe@example.com'
        },
        message: 'Doresc să devin proprietar pentru a-mi lista proprietatea de vacanță din Brașov.',
        status: 'PENDING'
    };

    const generateSampleLinks = () => {
        const approvalLink = ownerApplicationService.generateApprovalLink(sampleApplication.id, sampleApplication.submittedAt);
        const rejectionLink = ownerApplicationService.generateRejectionLink(sampleApplication.id, sampleApplication.submittedAt);
        
        return { approvalLink, rejectionLink };
    };

    const { approvalLink, rejectionLink } = generateSampleLinks();

    const copyToClipboard = async (text, label) => {
        const success = await ownerApplicationService.copyToClipboard(text);
        if (success) {
            alert(`${label} copiat în clipboard!`);
        } else {
            alert('Nu s-a putut copia. Folosește Ctrl+C manual.');
        }
    };

    const tabs = [
        { id: 'overview', label: '📋 Prezentare Generală' },
        { id: 'demo', label: '🎯 Demo Link-uri' },
        { id: 'dashboard', label: '🔧 Dashboard Admin' },
        { id: 'instructions', label: '📚 Instrucțiuni' }
    ];

    return (
        <div className="min-h-screen p-4">
            <BackgroundLayer />
            <div className="relative z-10 max-w-6xl mx-auto">
                
                {/* Header */}
                <div className="text-center mb-8">
                    <h1 className="text-4xl font-bold text-white mb-4">
                        🔐 Sistem de Aprobare Manuală
                    </h1>
                    <p className="text-xl text-blue-200">
                        Administrare sigură a cererilor de proprietar
                    </p>
                </div>

                {/* Navigation Tabs */}
                <div className="flex flex-wrap justify-center gap-2 mb-8">
                    {tabs.map(tab => (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTab(tab.id)}
                            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                                activeTab === tab.id
                                    ? 'bg-blue-600 text-white'
                                    : 'bg-white/20 text-blue-200 hover:bg-white/30'
                            }`}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>

                {/* Tab Content */}
                {activeTab === 'overview' && (
                    <div className="space-y-6">
                        <GlassCard>
                            <h2 className="text-2xl font-bold text-white mb-4">🎯 Funcționalități Implementate</h2>
                            <div className="grid md:grid-cols-2 gap-6">
                                <div>
                                    <h3 className="text-lg font-semibold text-white mb-3">🔒 Securitate</h3>
                                    <ul className="space-y-2 text-blue-100">
                                        <li>✅ Protecție cu parolă administratorului</li>
                                        <li>✅ Token-uri de securitate unice</li>
                                        <li>✅ Procesare una singură dată</li>
                                        <li>✅ Logare acțiuni administrative</li>
                                        <li>✅ Validare completă input</li>
                                    </ul>
                                </div>
                                <div>
                                    <h3 className="text-lg font-semibold text-white mb-3">📧 Notificări Email</h3>
                                    <ul className="space-y-2 text-blue-100">
                                        <li>✅ Email automat la aprobare</li>
                                        <li>✅ Email automat la respingere</li>
                                        <li>✅ Template-uri profesionale</li>
                                        <li>✅ Informații complete despre decizie</li>
                                    </ul>
                                </div>
                            </div>
                        </GlassCard>

                        <GlassCard>
                            <h2 className="text-2xl font-bold text-white mb-4">🔗 Structura Link-urilor</h2>
                            <div className="bg-white/10 rounded-lg p-4 mb-4">
                                <h3 className="text-lg font-semibold text-white mb-2">Format:</h3>
                                <code className="text-green-300 text-sm block mb-2">
                                    /owner-application/respond?applicationId=123&action=approve&token=abc123
                                </code>
                                <code className="text-red-300 text-sm block">
                                    /owner-application/respond?applicationId=123&action=reject&token=abc123
                                </code>
                            </div>
                            <div className="grid md:grid-cols-3 gap-4 text-sm">
                                <div className="bg-white/5 rounded p-3">
                                    <h4 className="font-semibold text-white">applicationId</h4>
                                    <p className="text-blue-200">ID-ul cererii din baza de date</p>
                                </div>
                                <div className="bg-white/5 rounded p-3">
                                    <h4 className="font-semibold text-white">action</h4>
                                    <p className="text-blue-200">"approve" sau "reject"</p>
                                </div>
                                <div className="bg-white/5 rounded p-3">
                                    <h4 className="font-semibold text-white">token</h4>
                                    <p className="text-blue-200">Token de securitate unic</p>
                                </div>
                            </div>
                        </GlassCard>
                    </div>
                )}

                {activeTab === 'demo' && (
                    <div className="space-y-6">
                        <GlassCard>
                            <h2 className="text-2xl font-bold text-white mb-4">🎯 Demo Link-uri de Aprobare</h2>
                            <div className="bg-white/10 rounded-lg p-4 mb-6">
                                <h3 className="text-lg font-semibold text-white mb-3">📝 Cerere de Exemplu:</h3>
                                <div className="grid md:grid-cols-2 gap-4 text-sm">
                                    <div>
                                        <p><strong className="text-blue-200">Solicitant:</strong> {sampleApplication.user.firstName} {sampleApplication.user.lastName}</p>
                                        <p><strong className="text-blue-200">Email:</strong> {sampleApplication.user.email}</p>
                                        <p><strong className="text-blue-200">ID Cerere:</strong> {sampleApplication.id}</p>
                                    </div>
                                    <div>
                                        <p><strong className="text-blue-200">Status:</strong> <span className="bg-yellow-500/20 text-yellow-200 px-2 py-1 rounded">{sampleApplication.status}</span></p>
                                        <p><strong className="text-blue-200">Data:</strong> {new Date(sampleApplication.submittedAt).toLocaleDateString('ro-RO')}</p>
                                    </div>
                                </div>
                                <div className="mt-3">
                                    <p><strong className="text-blue-200">Mesaj:</strong></p>
                                    <p className="text-blue-100 italic">"{sampleApplication.message}"</p>
                                </div>
                            </div>

                            <div className="space-y-4">
                                <div className="bg-green-500/10 border border-green-500/30 rounded-lg p-4">
                                    <h4 className="text-lg font-semibold text-white mb-2">✅ Link Aprobare</h4>
                                    <div className="bg-black/20 rounded p-3 mb-3">
                                        <code className="text-green-300 text-xs break-all">{approvalLink}</code>
                                    </div>
                                    <div className="flex space-x-2">
                                        <a
                                            href={approvalLink}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded font-medium transition-colors"
                                        >
                                            🔗 Testează Link
                                        </a>
                                        <button
                                            onClick={() => copyToClipboard(approvalLink, 'Link-ul de aprobare')}
                                            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded font-medium transition-colors"
                                        >
                                            📋 Copiază
                                        </button>
                                    </div>
                                </div>

                                <div className="bg-red-500/10 border border-red-500/30 rounded-lg p-4">
                                    <h4 className="text-lg font-semibold text-white mb-2">❌ Link Respingere</h4>
                                    <div className="bg-black/20 rounded p-3 mb-3">
                                        <code className="text-red-300 text-xs break-all">{rejectionLink}</code>
                                    </div>
                                    <div className="flex space-x-2">
                                        <a
                                            href={rejectionLink}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded font-medium transition-colors"
                                        >
                                            🔗 Testează Link
                                        </a>
                                        <button
                                            onClick={() => copyToClipboard(rejectionLink, 'Link-ul de respingere')}
                                            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded font-medium transition-colors"
                                        >
                                            📋 Copiază
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-yellow-500/10 border border-yellow-500/30 rounded-lg p-4 mt-6">
                                <h4 className="font-semibold text-white mb-2">⚠️ Notă de Testare</h4>
                                <p className="text-yellow-200 text-sm">
                                    Link-urile de mai sus sunt pentru demonstrație și testare. Când faci clic pe ele, 
                                    vei fi redirecționat către pagina de confirmare unde poți testa întreaga funcționalitate.
                                    <br/><br/>
                                    <strong>Parola pentru testare:</strong> <code className="bg-white/10 px-1 rounded">Rzvtare112</code>
                                </p>
                            </div>
                        </GlassCard>
                    </div>
                )}

                {activeTab === 'dashboard' && (
                    <div>
                        <AdminOwnerApplicationsPanel />
                    </div>
                )}

                {activeTab === 'instructions' && (
                    <div className="space-y-6">
                        <GlassCard>
                            <h2 className="text-2xl font-bold text-white mb-4">📚 Ghid de Utilizare</h2>
                            
                            <div className="space-y-6">
                                <div>
                                    <h3 className="text-lg font-semibold text-white mb-3">🔧 Pentru Administratori</h3>
                                    <div className="space-y-3 text-blue-100">
                                        <div className="bg-white/5 rounded p-3">
                                            <h4 className="font-semibold text-white mb-2">Metoda 1: Dashboard Admin</h4>
                                            <ol className="list-decimal list-inside space-y-1 text-sm">
                                                <li>Accesează tab-ul "Dashboard Admin"</li>
                                                <li>Vezi lista cu cereri în așteptare</li>
                                                <li>Fă clic pe "✅ Aprobare" sau "❌ Respingere"</li>
                                                <li>Introdu parola: <code className="bg-white/10 px-1 rounded">Rzvtare112</code></li>
                                                <li>Confirmă acțiunea</li>
                                            </ol>
                                        </div>

                                        <div className="bg-white/5 rounded p-3">
                                            <h4 className="font-semibold text-white mb-2">Metoda 2: Link-uri din Email</h4>
                                            <ol className="list-decimal list-inside space-y-1 text-sm">
                                                <li>Primești email de notificare pentru cerere nouă</li>
                                                <li>Fă clic pe link-ul de aprobare/respingere din email</li>
                                                <li>Introdu parola administratorului</li>
                                                <li>Confirmă decizia</li>
                                            </ol>
                                        </div>

                                        <div className="bg-white/5 rounded p-3">
                                            <h4 className="font-semibold text-white mb-2">Metoda 3: Partajare Link-uri</h4>
                                            <ol className="list-decimal list-inside space-y-1 text-sm">
                                                <li>Folosește dashboard-ul pentru a copia link-uri sigure</li>
                                                <li>Trimite link-urile prin email/chat altor administratori</li>
                                                <li>Destinatarul face clic pe link și introduce parola</li>
                                                <li>Procesează cererea</li>
                                            </ol>
                                        </div>
                                    </div>
                                </div>

                                <div>
                                    <h3 className="text-lg font-semibold text-white mb-3">👤 Pentru Solicitanți</h3>
                                    <div className="bg-white/5 rounded p-3">
                                        <ol className="list-decimal list-inside space-y-1 text-blue-100 text-sm">
                                            <li>Înregistrează-te ca oaspete în aplicație</li>
                                            <li>Trimite cererea de proprietar cu un mesaj explicativ</li>
                                            <li>Așteaptă notificarea email cu decizia administratorilor</li>
                                            <li>Dacă ești aprobat: acces la funcționalitățile de proprietar</li>
                                            <li>Dacă ești respins: actualizează profilul și reaplică</li>
                                        </ol>
                                    </div>
                                </div>

                                <div>
                                    <h3 className="text-lg font-semibold text-white mb-3">🔒 Măsuri de Securitate</h3>
                                    <div className="grid md:grid-cols-2 gap-4 text-sm">
                                        <div className="bg-green-500/10 rounded p-3">
                                            <h4 className="font-semibold text-white mb-2">✅ Implementate</h4>
                                            <ul className="space-y-1 text-green-200">
                                                <li>• Protecție cu parolă pentru toate acțiunile</li>
                                                <li>• Token-uri unice pentru fiecare link</li>
                                                <li>• Prevenirea procesării duble</li>
                                                <li>• Logare completă a acțiunilor</li>
                                                <li>• Validare completă a input-urilor</li>
                                            </ul>
                                        </div>
                                        <div className="bg-blue-500/10 rounded p-3">
                                            <h4 className="font-semibold text-white mb-2">📋 Recomandări</h4>
                                            <ul className="space-y-1 text-blue-200">
                                                <li>• Nu partaja parola administratorului</li>
                                                <li>• Verifică identitatea solicitantului</li>
                                                <li>• Documentează motivele respingerii</li>
                                                <li>• Monitorizează logurile aplicației</li>
                                                <li>• Schimbă parola periodic</li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </GlassCard>

                        <GlassCard>
                            <h2 className="text-2xl font-bold text-white mb-4">🚨 Depanare</h2>
                            <div className="space-y-4 text-sm">
                                <div className="bg-red-500/10 rounded p-3">
                                    <h4 className="font-semibold text-white mb-2">❌ Probleme Comune</h4>
                                    <div className="space-y-2 text-red-200">
                                        <p><strong>"Invalid token":</strong> Link-ul a expirat sau a fost modificat - generează unul nou</p>
                                        <p><strong>"Parolă incorectă":</strong> Folosește parola corectă: <code className="bg-white/10 px-1 rounded">Rzvtare112</code></p>
                                        <p><strong>"Application already processed":</strong> Cererea a fost deja procesată</p>
                                        <p><strong>"Email not sent":</strong> Verifică configurația email din <code className="bg-white/10 px-1 rounded">application.properties</code></p>
                                    </div>
                                </div>
                                <div className="bg-blue-500/10 rounded p-3">
                                    <h4 className="font-semibold text-white mb-2">🔍 Debugging</h4>
                                    <div className="space-y-1 text-blue-200">
                                        <p>• Verifică logurile backend pentru detalii despre erori</p>
                                        <p>• Folosește DevTools pentru a inspectiona cererile de rețea</p>
                                        <p>• Rulează scriptul de testare: <code className="bg-white/10 px-1 rounded">test-approval-system.bat</code></p>
                                        <p>• Verifică statusul aplicației în baza de date</p>
                                    </div>
                                </div>
                            </div>
                        </GlassCard>
                    </div>
                )}

                {/* Back to Home */}
                <div className="text-center mt-8">
                    <PrimaryButton onClick={() => window.location.href = '/'}>
                        🏠 Înapoi la Pagina Principală
                    </PrimaryButton>
                </div>
            </div>
        </div>
    );
};

export default AdminDemoPage;
