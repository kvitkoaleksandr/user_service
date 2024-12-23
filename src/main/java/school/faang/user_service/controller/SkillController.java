package school.faang.user_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.faang.user_service.dto.skill.SkillCandidateDto;
import school.faang.user_service.dto.skill.SkillDto;
import school.faang.user_service.service.SkillService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/skill")
public class SkillController {
    private final SkillService service;

    @PostMapping // Создание скилла
    public SkillDto create(@RequestBody SkillDto skillDto) {
        return service.createSkill(skillDto);
    }

    @GetMapping // Получение списка скиллов пользователя
    public List<SkillDto> getUserSkills(@RequestParam long userId) {
        return service.getUserSkills(userId);
    }

    @GetMapping("/offers") // Получение предложенных скиллов
    public List<SkillCandidateDto> getOfferedSkills(@RequestParam long userId) {
        return service.getOfferedSkills(userId);
    }

    @PutMapping("/acquire") // Приобретение предложенного скилла
    public SkillDto acquireSkillFromOffers(@RequestParam long skillId, @RequestParam long userId) {
        return service.acquireSkillFromOffers(skillId, userId);
    }
}
