package com.demo;

public class App {
    public static String saluer(String nom) {
    if (nom == null || nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }
        return "Bonjour, " + nom + " !  ";
    }
    public static void main(String[] args) {
        System.out.println(saluer("Jenkins"));
    }
}
