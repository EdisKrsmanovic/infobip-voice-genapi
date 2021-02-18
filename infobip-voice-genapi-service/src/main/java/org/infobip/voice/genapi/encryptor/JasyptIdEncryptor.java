package org.infobip.voice.genapi.encryptor;

import com.google.common.io.BaseEncoding;
import org.infobip.voice.genapi.exception.InvalidIdException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JasyptIdEncryptor {

    private final BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();

    public JasyptIdEncryptor() {
//TODO: get password from somewhere else
        basicTextEncryptor.setPassword("randomPassword");
    }

    public String encryptId(Integer id) {
        String encryptedString = basicTextEncryptor.encrypt(id.toString());
        return encodeUsingGuava(encryptedString);
    }

    public Integer decryptId(String encryptedHex) throws InvalidIdException {
        try {
            String encryptedString = decodeUsingGuava(encryptedHex);
            return Integer.valueOf(basicTextEncryptor.decrypt(encryptedString));
        } catch (Exception e) {
            throw new InvalidIdException("Invalid id");
        }
    }

    private String encodeUsingGuava(String encryptedString) {
        return BaseEncoding.base16().encode(encryptedString.getBytes(StandardCharsets.UTF_8));
    }

    public String decodeUsingGuava(String encryptedHex) {
        return new String(BaseEncoding.base16()
                .decode(encryptedHex.toUpperCase()));
    }
}
