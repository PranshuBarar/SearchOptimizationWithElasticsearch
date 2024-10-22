package com.searchoptimizationv2.search_optimization_application_v2.controllers;

import com.searchoptimizationv2.search_optimization_application_v2.DTOs.NoteDTO;
import com.searchoptimizationv2.search_optimization_application_v2.DTOs.NoteUpdateRequest;
import com.searchoptimizationv2.search_optimization_application_v2.Service.NoteService;
import com.searchoptimizationv2.search_optimization_application_v2.entities.Note;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/note")
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/createNote")
    public String createNote(@RequestBody NoteDTO noteDTO) throws IOException {
       return noteService.createNote(noteDTO);
    }

    @PutMapping("/updateNote")
    public String updateNote(@RequestBody NoteUpdateRequest noteUpdateRequest) throws IOException {
        return noteService.updateNote(noteUpdateRequest);
    }

    @DeleteMapping("/deleteNote")
    public String deleteNote(@RequestBody String noteIdString) throws IOException {
        return noteService.deleteNote(noteIdString);
    }

    @GetMapping("/getNote")
    public Note getNote(@RequestParam("noteId") String noteIdString){
        return noteService.getNote(noteIdString);
    }

//    @GetMapping("/searchNote")
//    public List<Note> searchNote(@RequestBody Filter filter) throws IOException {
//        return noteService.searchNote(filter);
//    }
}
