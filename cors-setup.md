# CORS backoffice -> frontoffice

## Pourquoi

Le frontoffice tourne sur http://localhost:5173 (Vite). Quand il appelle l'API Spring Boot (http://localhost:8080), le navigateur bloque par defaut (policy CORS). Il faut autoriser explicitement l'origine front.

## Ce que j'ai ajoute

Dans [back/src/main/java/com/example/demo/config/WebConfig.java](back/src/main/java/com/example/demo/config/WebConfig.java), j'ai ajoute une configuration CORS globale sur les endpoints /api/** :

- Origine autorisee : http://localhost:5173
- Methodes autorisees : GET, POST, PUT, PATCH, DELETE, OPTIONS
- Headers : tous
- Credentials : true (si tu utilises des cookies/headers d'auth plus tard)

## Comment cela marche

Spring appelle addCorsMappings(...) au demarrage. Chaque requete vers /api/** qui vient de http://localhost:5173 sera acceptee par le navigateur.

## Si tu changes le port

Si tu changes le port Vite, remplace simplement l'origine dans WebConfig.java :

```java
.allowedOrigins("http://localhost:NOUVEAU_PORT")
```

## Alternative rapide (annotations)

Tu peux aussi mettre @CrossOrigin sur un controller ou une methode, mais la config globale est plus pratique pour un projet entier.
