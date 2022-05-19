package com.group;

import com.group.model.dto.GroupDto;
import com.user.UserService;
import com.user.dto.UserListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.UrlMapping.*;

@RestController
@RequestMapping(GROUPS)
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;

    @CrossOrigin
    @GetMapping
    public List<GroupDto> allGroups() {
        return groupService.findAll();
    }

    @CrossOrigin
    @PatchMapping(ENTITY)
    public void addUser(@PathVariable Long id, @RequestBody GroupDto groupDto) {
        userService.addToGroup(id, groupDto);
    }

    @CrossOrigin
    @GetMapping(ENTITY)
    public GroupDto getGroup(@PathVariable Long id) {
        return groupService.get(id);
    }
}
