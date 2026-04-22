package org.example.aicareernav1.service.testService;

import lombok.extern.slf4j.Slf4j;
import org.example.aicareernav1.dto.testDto.CodeExecutionResult;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class CodeExecutionService {

    public CodeExecutionResult execute(String code, String lang) {
        // Если пришло "none", выполнение даже не начинается
        if ("none".equals(lang)) {
            return CodeExecutionResult.builder()
              .stderr("Compiler not available for this role")
              .detectedLanguage("none")
              .build();
        }

        String fileName;
        String imageName;
        String runCmd;

        // Только конкретные соответствия
        switch (lang) {
            case "java" -> { fileName = "Main.java"; imageName = "sandbox-java"; runCmd = "javac Main.java && java Main"; }
            case "python" -> { fileName = "main.py"; imageName = "sandbox-python"; runCmd = "python3 main.py"; }
            case "javascript" -> { fileName = "main.js"; imageName = "sandbox-javascript"; runCmd = "node main.js"; }
            case "cpp" -> { fileName = "main.cpp"; imageName = "sandbox-cpp"; runCmd = "g++ main.cpp -o main && ./main"; }
            case "ruby" -> { fileName = "main.rb"; imageName = "sandbox-ruby"; runCmd = "ruby main.rb"; }
            case "go" -> { fileName = "main.go"; imageName = "sandbox-go"; runCmd = "go run main.go"; }
            case "php" -> { fileName = "main.php"; imageName = "sandbox-php"; runCmd = "php main.php"; }
            case "csharp" -> { fileName = "Program.cs"; imageName = "sandbox-csharp"; runCmd = "dotnet new console --force && dotnet run"; }
            default -> {
                return CodeExecutionResult.builder().stderr("Unsupported language").detectedLanguage("none").build();
            }
        }

        return runInDocker(code, fileName, imageName, runCmd, lang);
    }

    private CodeExecutionResult runInDocker(String code, String file, String img, String cmd, String lang) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("sandbox_");
            Files.writeString(tempDir.resolve(file), code);
            String[] dockerCmd = {"docker", "run", "--rm", "--network", "none", "--memory", "128m", "-v", tempDir.toAbsolutePath() + ":/app:rw", img, "sh", "-c", cmd};
            Process proc = new ProcessBuilder(dockerCmd).start();
            proc.getOutputStream().close();
            CompletableFuture<String> out = CompletableFuture.supplyAsync(() -> readStream(proc.getInputStream()));
            CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> readStream(proc.getErrorStream()));
            if (!proc.waitFor(15, TimeUnit.SECONDS)) { // 15с для C#
                proc.destroyForcibly();
                return CodeExecutionResult.builder().isTimeout(true).detectedLanguage(lang).build();
            }
            return CodeExecutionResult.builder().stdout(out.get()).stderr(err.get()).detectedLanguage(lang).build();
        } catch (Exception e) {
            return CodeExecutionResult.builder().stderr(e.getMessage()).build();
        } finally {
            if (tempDir != null) deleteDir(tempDir.toFile());
        }
    }

    private String readStream(InputStream is) {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            return r.lines().reduce("", (a, b) -> a + b + "\n");
        } catch (IOException e) { return ""; }
    }

    private void deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) for (File f : contents) deleteDir(f);
        file.delete();
    }
}