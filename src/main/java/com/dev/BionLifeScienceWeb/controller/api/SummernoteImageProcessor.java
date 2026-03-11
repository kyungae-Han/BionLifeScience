package com.dev.BionLifeScienceWeb.controller.api;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SummernoteImageProcessor {

    @Value("${spring.upload.path}")
    private String commonPath;

    public String processEditorImages(String desc, String type) throws IOException {
        if (desc == null || !desc.contains("data:image")) return desc;

        Pattern pattern = Pattern.compile(
            "<img[^>]*src=[\"']data:image/([a-zA-Z0-9+]+);base64,([^\"']+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(desc);

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        String uploadDir;
        String webPath;

        uploadDir = commonPath + "/" + type + "/editor/" + today;
        webPath = "/upload/" + type + "/editor/" + today;

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String ext = matcher.group(1).toLowerCase();
            String base64 = matcher.group(2).replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(base64);

            if ("jpeg".equals(ext)) ext = "jpg";

            String fileName = UUID.randomUUID() + "." + ext;
            File target = new File(dir, fileName);

            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(decoded);
            }

            String oldTag = matcher.group(0);
            String newTag = oldTag.replaceFirst(
                "src=[\"'][^\"']+[\"']",
                "src='" + webPath + "/" + fileName + "'"
            );

            matcher.appendReplacement(sb, Matcher.quoteReplacement(newTag));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
