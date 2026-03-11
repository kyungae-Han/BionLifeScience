package com.dev.BionLifeScienceWeb.utils;

public class PasswordEncodeTest {
    public static void main(String[] args) {
        PasswordEncoding encoder = new PasswordEncoding();
        String encoded = encoder.encode("bion2272"); // ← 원하는 비밀번호 입력
        //System.out.println("Encoded Password: " + encoded);
    }
}