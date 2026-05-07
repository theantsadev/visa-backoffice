package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.metier.DemandeListeService;
import com.example.demo.metier.dto.DemandeDetailDTO;
import com.example.demo.metier.dto.DemandeListeItemDTO;
import com.example.demo.metier.dto.DemandeSuiviDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

@RestController
@RequestMapping("/api/demandes")
public class DemandeListeController {

    private final DemandeListeService demandeListeService;

    public DemandeListeController(DemandeListeService demandeListeService) {
        this.demandeListeService = demandeListeService;
    }

    @GetMapping
    public List<DemandeListeItemDTO> listerToutesLesDemandes() {
        return demandeListeService.listerToutesLesDemandes();
    }

    @GetMapping("/{id}")
    public DemandeDetailDTO getDetailDemande(@PathVariable("id") Integer idDemande) {
        return demandeListeService.getDetailDemande(idDemande);
    }

    @GetMapping("/suivi/{numeroId}")
    public List<DemandeSuiviDTO> suivreDemande(@PathVariable String numeroId) {
        return demandeListeService.suivreDemande(numeroId);
    }

    @GetMapping(value = "/{numeroId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] genererQrSuivi(@PathVariable String numeroId,
            @RequestParam(name = "size", defaultValue = "220") int size,
            HttpServletRequest request) {
        String foBaseUrl = "http://localhost:5173";
        int qrSize = Math.min(Math.max(size, 160), 600);
        String trackingUrl = foBaseUrl + "/demande/suivi/" + numeroId;

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 1);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(trackingUrl, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | java.io.IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impossible de generer le QR de suivi", ex);
        }
    }
}
