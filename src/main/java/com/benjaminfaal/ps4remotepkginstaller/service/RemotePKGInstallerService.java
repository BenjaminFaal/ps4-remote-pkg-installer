package com.benjaminfaal.ps4remotepkginstaller.service;

import com.benjaminfaal.ps4remotepkginstaller.model.api.request.InstallRequest;
import com.benjaminfaal.ps4remotepkginstaller.model.api.response.ExistsResponse;
import com.benjaminfaal.ps4remotepkginstaller.model.api.response.InstallResponse;
import com.benjaminfaal.ps4remotepkginstaller.model.api.response.ModifyTaskResponse;
import com.benjaminfaal.ps4remotepkginstaller.model.api.response.TaskProgress;

import java.io.File;
import java.util.List;

public interface RemotePKGInstallerService {

    void setHost(String host);

    boolean isRunning();

    ExistsResponse exists(String titleId);

    InstallResponse installFiles(File[] files);

    InstallResponse installManifestJSONUrl(String manifestJsonUrl);

    InstallResponse installPKGUrl(String pkgUrl);

    ModifyTaskResponse stopTask(Integer id);

    ModifyTaskResponse pauseTask(Integer id);

    ModifyTaskResponse resumeTask(Integer id);

    ModifyTaskResponse removeTask(Integer id);

    List<TaskProgress> getTasks();

    List<Integer> getTaskIds();

    InstallRequest getInstallRequest(Integer taskId);

    TaskProgress getTask(Integer id);

    TaskProgress getCachedTask(Integer id);

    int countTasks();

}
