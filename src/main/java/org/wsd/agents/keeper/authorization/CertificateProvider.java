package org.wsd.agents.keeper.authorization;

public class CertificateProvider {
    // TODO real encription will be implemented in release version, encription do not imlemented during POC phase
    private static String publicKey = new String ("publicKey");
    private static String privateKey = new String("privateKey");

    private ERESGate eres = ERESGate.getInstance();

    public String generateCertificate(String email, String password) {
        String authorizationLevel = eres.getAuthorizationLevel(email, password);
        String certificate = new StringBuilder(privateKey).append(authorizationLevel).toString();
        return certificate;
    }

    public String getPermissions() {
        return new String(publicKey);
    }
}
