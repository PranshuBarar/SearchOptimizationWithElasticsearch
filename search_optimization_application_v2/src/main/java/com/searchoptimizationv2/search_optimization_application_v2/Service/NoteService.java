package com.searchoptimizationv2.search_optimization_application_v2.Service;

import com.optimization_component.payload.Payload;
import com.optimization_component.service.ElasticSearchServiceImpl;
import com.searchoptimizationv2.search_optimization_application_v2.DTOs.NoteDTO;
import com.searchoptimizationv2.search_optimization_application_v2.DTOs.NoteUpdateRequest;
import com.searchoptimizationv2.search_optimization_application_v2.entities.Note;
import com.searchoptimizationv2.search_optimization_application_v2.entities.User;
import com.searchoptimizationv2.search_optimization_application_v2.repository.NoteRepository;
import com.searchoptimizationv2.search_optimization_application_v2.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class NoteService{
    
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private ElasticSearchServiceImpl<Note> elasticSearchService;

    public String createNote(NoteDTO noteDTO) throws IOException {

        UUID userId = UUID.fromString(noteDTO.getUserIdString());

        User user = userRepository.findById(userId).orElseThrow();

        Note note = Note.builder()
                .note(noteDTO.getNote())
                .owner(user)
                .build();

        Note noteFromDB = noteRepository.save(note);

        //==========================================================================
        elasticSearchService.save(Note.class.getName().toLowerCase(), noteFromDB.getNoteId().toString(), noteFromDB);
        //==========================================================================

        return "Note created successfully";
    }

    public String updateNote(NoteUpdateRequest noteUpdateRequest) throws IOException {
        String noteIdString = noteUpdateRequest.getNoteIdString();
        String updatedNote = noteUpdateRequest.getUpdatedNote();
        UUID noteId = UUID.fromString(noteIdString);
        Note note = noteRepository.findById(noteId).orElseThrow();
        note.setNote(updatedNote);
        noteRepository.save(note);

        //==========================================================================
        elasticSearchService.save(Note.class.getName().toLowerCase(), noteIdString, note);
        //==========================================================================

        return "Note updated successfully";
    }

    public String deleteNote(String noteIdString) throws IOException {
        UUID noteId = UUID.fromString(noteIdString);
        noteRepository.deleteById(noteId);

        //==========================================================================
        elasticSearchService.delete(Note.class.getName().toLowerCase(), noteIdString);
        //==========================================================================

        return "Note deleted successfully";
    }

    public Note getNote(String noteIdString){
        UUID noteId = UUID.fromString(noteIdString);
        return noteRepository.findById(noteId).orElseThrow();
    }

    public List<Note> searchNote(Payload payload) throws IOException {
        return elasticSearchService.search(Note.class.getName().toLowerCase(), payload);
    }
}
