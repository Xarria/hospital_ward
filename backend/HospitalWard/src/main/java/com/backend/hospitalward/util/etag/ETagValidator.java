package com.backend.hospitalward.util.etag;

import com.backend.hospitalward.exception.ErrorKey;
import com.backend.hospitalward.exception.UnknownException;
import com.backend.hospitalward.security.SecurityConstants;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import java.text.ParseException;

import static com.nimbusds.jose.JWSAlgorithm.HS512;

public class ETagValidator {

    public static String calculateDTOSignature(SignableDTO signableDTO) {
        try {
            JWSSigner signerJWS = new MACSigner(SecurityConstants.SECRET);
            JWSObject objectJWS = new JWSObject(new JWSHeader(HS512), new Payload(String.valueOf(signableDTO.getSignablePayload())));
            objectJWS.sign(signerJWS);

            return objectJWS.serialize();

        } catch (JOSEException ex) {
            throw new UnknownException(ErrorKey.UNKNOWN);
        }
    }

    public static boolean validateDTOSignature(String header) {

        try {
            JWSObject objectJWS = JWSObject.parse(header);
            JWSVerifier verifier = new MACVerifier(SecurityConstants.SECRET);

            return !objectJWS.verify(verifier);

        } catch (JOSEException | ParseException ex) {
            return true;
        }
    }

    public static boolean verifyDTOIntegrity(String header, SignableDTO signableDTO) {
        try {
            final String ifMatchHeaderValue = JWSObject.parse(header).getPayload().toString();
            final String entitySignablePayloadValue = signableDTO.getSignablePayload().toString();

            return validateDTOSignature(header) || !ifMatchHeaderValue.equals(entitySignablePayloadValue);
        } catch (ParseException ex) {
            return true;
        }
    }
}
