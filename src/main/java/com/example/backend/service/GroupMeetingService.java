package com.example.backend.service;

import com.example.backend.model.GroupMeeting;
import com.example.backend.repository.GroupMeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupMeetingService {

    @Autowired
    private GroupMeetingRepository groupMeetingRepository;

    // Lấy tất cả GroupMeeting
    public List<GroupMeeting> getAllGroupMeetings() {
        return groupMeetingRepository.findAll();
    }

    // Lấy GroupMeeting theo ID
    public GroupMeeting getGroupMeetingById(Integer id) {
        Optional<GroupMeeting> optional = groupMeetingRepository.findById(id);
        return optional.orElse(null);
    }

    // Tạo mới GroupMeeting
    public GroupMeeting createGroupMeeting(GroupMeeting groupMeeting) {
        return groupMeetingRepository.save(groupMeeting);
    }

    // Lưu (cập nhật) GroupMeeting sau khi thêm người tham gia
    public GroupMeeting saveGroupMeeting(GroupMeeting groupMeeting) {
        return groupMeetingRepository.save(groupMeeting);
    }

    // Xoá GroupMeeting
    public boolean deleteGroupMeeting(Integer id) {
        if (groupMeetingRepository.existsById(id)) {
            groupMeetingRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
