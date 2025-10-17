package com.mod5.evalfinal_gestareav4;

import java.util.UUID;

public class UuidGenerator {
    // Método simple para mostrar la convivencia con JAVA, genera un ID único (UUID).
    // Heredado
    // @return UUID
    public static String generateUniqueId() {
        // Genera un UUID aleatorio y lo convierte a String.
        return UUID.randomUUID().toString();
    }
}