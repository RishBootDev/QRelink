package org.rishbootdev.qrelink.controller;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.rishbootdev.qrelink.dtos.ClickEventDTO;
import org.rishbootdev.qrelink.dtos.UrlMappingDTO;
import org.rishbootdev.qrelink.models.UrlMapping;
import org.rishbootdev.qrelink.models.User;
import org.rishbootdev.qrelink.service.QrCodeService;
import org.rishbootdev.qrelink.service.UrlMappingService;
import org.rishbootdev.qrelink.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlMappingController {
    private final UrlMappingService urlMappingService;
    private final UserService userService;
    private final QrCodeService qrCodeService;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody Map<String, String> request,
                                                        Principal principal){
        String originalUrl = request.get("originalUrl");
        User user = userService.findByUsername(principal.getName());
        UrlMappingDTO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, user);
        return ResponseEntity.ok(urlMappingDTO);
    }


    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUserUrls(Principal principal){
        User user = userService.findByUsername(principal.getName());
        List<UrlMappingDTO> urls = urlMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urls);
    }


    @GetMapping("/analytics/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDTO>> getUrlAnalytics(@PathVariable String shortUrl,
                                                               @RequestParam("startDate") String startDate,
                                                               @RequestParam("endDate") String endDate){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);
        List<ClickEventDTO> clickEventDTOS = urlMappingService.getClickEventsByDate(shortUrl, start, end);
        return ResponseEntity.ok(clickEventDTOS);
    }


    @GetMapping("/totalClicks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate, Long>> getTotalClicksByDate(Principal principal,
                                                                     @RequestParam("startDate") String startDate,
                                                                     @RequestParam("endDate") String endDate){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        User user = userService.findByUsername(principal.getName());
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        Map<LocalDate, Long> totalClicks = urlMappingService.getTotalClicksByUserAndDate(user, start, end);
        return ResponseEntity.ok(totalClicks);
    }

    @GetMapping("/qr/{shortUrl}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String shortUrl) throws Exception {

        UrlMapping urlMapping = urlMappingService.getUrl(shortUrl);

        if (urlMapping == null) {
            return ResponseEntity.notFound().build();
        }

        String fullUrl = baseUrl + "/" + urlMapping.getShortUrl();
        byte[] qr = qrCodeService.generateQrCode(fullUrl, 300, 300);
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(qr);
    }


}
