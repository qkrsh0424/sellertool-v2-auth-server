package com.sellertool.auth_server.domain.workspace.controller;

import com.sellertool.auth_server.domain.message.dto.Message;
import com.sellertool.auth_server.domain.workspace.service.WorkspaceBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/workspace")
public class WorkspaceApiV1 {
    private final WorkspaceBusinessService workspaceBusinessService;

    @Autowired
    public WorkspaceApiV1(
            WorkspaceBusinessService workspaceBusinessService
    ) {
        this.workspaceBusinessService = workspaceBusinessService;
    }

    @GetMapping("")
    public ResponseEntity<?> searchWorkspace(@RequestParam Map<String, Object> params){
        Message message = new Message();

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        message.setData(workspaceBusinessService.searchWorkspace(params));

        return new ResponseEntity<>(message, message.getStatus());
    }
}
