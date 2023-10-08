package com.viktor.mono.service;

import com.viktor.mono.entity.Mcc;
import com.viktor.mono.repository.MccRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MccServiceTest {

    @Mock
    MccRepository mccRepository;

    @InjectMocks
    MccService mccService;

    @Test
    void fillsAllFromJsonIntoDatabase() {
        mccService.fillDatabaseFromFile();
        ArgumentCaptor<List<Mcc>> captor = ArgumentCaptor.forClass(List.class);
        verify(mccRepository).truncate();
        verify(mccRepository).saveAll(captor.capture());
        List<Mcc> value = captor.getValue();
        assertEquals(2, value.size());
        assertEquals(1520L, value.get(0).getCode());
        assertEquals("test1", value.get(0).getDescription());
        assertEquals(1711L, value.get(1).getCode());
        assertEquals("test2", value.get(1).getDescription());
    }

    @Test
    void returnsCorrectDescriptionForCode() {
        Mcc mcc = new Mcc();
        mcc.setCode(1L);
        mcc.setDescription("some descr");

        when(mccRepository.findByCode(1L)).thenReturn(mcc);

        String description = mccService.mccCodeToDescription(1L);
        assertEquals("some descr", description);
    }

    @Test
    void returnsDefaultValueIfMccNotFound() {
        when(mccRepository.findByCode(1L)).thenReturn(null);

        String description = mccService.mccCodeToDescription(1L);
        assertEquals("Не відомо", description);
    }
}