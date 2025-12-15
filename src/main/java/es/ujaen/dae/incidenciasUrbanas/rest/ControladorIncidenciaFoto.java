package es.ujaen.dae.incidenciasUrbanas.rest;

import es.ujaen.dae.incidenciasUrbanas.entidades.Incidencia;
import es.ujaen.dae.incidenciasUrbanas.repositorios.RepositorioIncidencias;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/incidencias")
public class ControladorIncidenciaFoto {

    @Autowired
    private RepositorioIncidencias repositorioIncidencias;

    @PostMapping("/{id}/foto")
    @Transactional
    public ResponseEntity<String> subirFoto(@PathVariable int id,
                                            @RequestParam("file") MultipartFile file) {
        try {
            Incidencia inc = repositorioIncidencias.buscarPorId(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            inc.setFoto(file.getBytes());
            repositorioIncidencias.actualizar(inc);

            return ResponseEntity.ok("Foto subida correctamente");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error leyendo el archivo");
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> descargarFoto(@PathVariable int id) {
        Incidencia inc = repositorioIncidencias.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        byte[] foto = inc.getFoto();
        if (foto == null) {
            return ResponseEntity.noContent().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(foto.length);

        return new ResponseEntity<>(foto, headers, HttpStatus.OK);
    }

}
