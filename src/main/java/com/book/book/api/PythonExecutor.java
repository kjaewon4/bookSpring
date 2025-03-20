package com.book.book.api;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class PythonExecutor {

    @EventListener(ApplicationReadyEvent.class)  // 스프링 애플리케이션 실행 완료 후 실행
    public void runPythonScript() {
        try {
            System.out.println("✅ Spring Boot 실행 완료! Python 실행 시작...");

            // 실행할 파이썬 파일 경로
            String pythonFilePath = "C:/study/workspace/project/main.py";

            // Python 실행
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonFilePath);
            processBuilder.environment().put("PYTHONIOENCODING", "UTF-8");

            processBuilder.redirectErrorStream(true);

            // 프로세스 실행
            Process process = processBuilder.start();

            // Python 실행 결과 및 오류 출력
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[PYTHON] " + line);
            }

            // Python 프로세스 종료 코드 확인
            int exitCode = process.waitFor();
            System.out.println("✅ Python script executed. Exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
