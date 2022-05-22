package com.user;

import com.group.GroupMapper;
import com.group.model.Group;
import com.group.model.dto.GroupDto;
import com.post.PostMapper;
import com.post.model.dto.PostDto;
import com.security.AuthService;
import com.security.dto.MessageResponse;
import com.security.dto.SignupRequest;
import com.user.dto.UserListDto;
import com.user.mapper.UserMapper;
import com.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;
    private final GroupMapper groupMapper;
    private final PostMapper postMapper;

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found" + id));
    }

    public ResponseEntity<MessageResponse> create(UserListDto user) {

        if (authService.existsByUsername(user.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (authService.existsByEmail(user.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        SignupRequest signUpRequest = new SignupRequest(user.getUsername(), user.getEmail(),
                user.getPassword(), user.getFirstName(), user.getLastName(), user.getAddress());

        authService.register(signUpRequest);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    public List<UserListDto> allUsersForList() {
        return userRepository.findAll()
                .stream().map(user -> {
                    UserListDto userListDto = userMapper.userListDtoFromUser(user);

                    Set <PostDto> postDtoSet = new HashSet<>();
                    user.getPosts().stream().map(postMapper::toDto).forEach(postDtoSet::add);

                    userMapper.populateFriends(user, userListDto);
                    userListDto.setPosts(postDtoSet);
                    return userListDto;
                })
                .collect(toList());
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public UserListDto edit(Long id, UserListDto user) {
        User userToUpdate = findById(id);
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setPassword(user.getPassword());
        userToUpdate.setUsername(user.getUsername());
        userToUpdate.setGroups(user.getGroups().stream().map(groupMapper::fromDto).collect(toSet()));
        return userMapper.userListDtoFromUser(userRepository.save(userToUpdate));
    }

    public UserListDto get(Long id) {
        User user = findById(id);
        UserListDto userListDto = userMapper.userListDtoFromUser(user);
        userMapper.populateFriends(user, userListDto);
        return userListDto;
    }


    public UserListDto addFriend(Long id, Long friendId) {
        User user = findById(id);
        User friend = findById(friendId);

        Set<User> friends = user.getFriends();
        friends.add(friend);
        user.setFriends(friends);

        Set<User> friendsOfFriend = friend.getFriends();
        friendsOfFriend.add(user);
        friend.setFriends(friendsOfFriend);

        System.out.println("user.getFriends() = " + user.getFriends());
        System.out.println("friend.getFriends() = " + friend.getFriends());

        userRepository.save(user);
        userRepository.save(friend);
        return userMapper.userListDtoFromUser(user);
    }


    public GroupDto addToGroup(Long id, GroupDto groupDto) {
        User user = findById(id);
        Group group = groupMapper.fromDto(groupDto);
        Set<Group> userGroups = user.getGroups();
        Set<Group> groups = new HashSet<>();
        if (userGroups != null) {
            groups.addAll(userGroups);
        }
        groups.add(group);
        user.setGroups(groups);
        userRepository.save(user);
        return groupMapper.toDto(group);
    }

    public Set<UserListDto> getFriends(Long id) {
        return get(id).getFriends();
    }
}