package com.example.demo.contoller;

import com.example.demo.util.Utills;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Controller
public class DeployContoller {

	@RequestMapping("/")
    public String deploy(){
        return "deploy";
    }
	@RequestMapping("/fileupload")
    public String fileupload(HttpServletRequest request,
                           @RequestParam("fileName")MultipartFile mFile,
                           @RequestParam("serverIp")String ip,
                           @RequestParam("serverPort")String port,
                           @RequestParam("accountID")String id,
                           @RequestParam("accountPW")String pw){
        if(updateAPK(mFile, ip,port,id,pw)){
            return "fileUpload";
        }else{
            return "deploy";
        }

    }

    private boolean updateAPK(MultipartFile mFile, String ip,String port,String id,String pw){
        try{
            InputStream reader = getClass().getResourceAsStream("/application.properties");
            Properties properties = new Properties();
            properties.load(reader);
            String downloadPath = properties.getProperty("downloadFolderPathName");
            File dPathFile = new File(downloadPath);
            if(!dPathFile.exists()){
                dPathFile.mkdir();
            }
            File downLoadFile = new File(downloadPath+mFile.getOriginalFilename());
            mFile.transferTo(downLoadFile);

            if(downLoadFile.exists()){
                //압축 풀기
                String unzipFolderName = mFile.getOriginalFilename().replace(".apk","");
                String unzipPath = downloadPath + unzipFolderName;
                if(Utills.getInstance().unZip(downloadPath, mFile.getOriginalFilename(), unzipPath)) {
                    System.out.println("ip   " + ip);
                    System.out.println("port   " + port);
                    System.out.println("id   " + id);
                    System.out.println("pw   " + pw);
                    //config 데이터 만들기
                    String configTxt = getConfigData(ip, port, id, pw);
                    System.out.println("configTxt   " + configTxt);
                    File configFile = new File(unzipPath + "/assets/config.txt");
                    // 파일 asset 폴더에 저장
                    if (Utills.getInstance().createFile(configTxt, configFile)) {
                        // sign 폴더 삭제
                        File signFolder = new File(unzipPath + "/META-INF");
                        if (!Utills.getInstance().deleteFolder(signFolder)) {
                            // 재 압축
                            String reApkFile = unzipFolderName + "-resign.apk";
                            File beforeApkFile = new File(downloadPath + reApkFile);
                            if(beforeApkFile.exists()){
                                beforeApkFile.delete();
                            }

                            if (reApkZip(downloadPath + "zip.exe", reApkFile, unzipPath)) {
                                //재 sign
                                boolean isResult = reSignApk(downloadPath, downloadPath + reApkFile);
                                if(isResult){
                                    Utills.getInstance().deleteFolder(new File(unzipPath));
                                    downLoadFile.delete();
                                    Utills.getInstance().setDownloadFile(new File(downloadPath + reApkFile));
                                    System.out.println("downloadFile   " + Utills.getInstance().getDownloadFile());
                                    return true;
                                }else{
                                    return false;
                                }

                            }else{
                                System.out.println("재 압축 실패");
                            }
                        } else {
                            System.out.println("sign 폴더 삭제 실패");
                        }
                    } else {
                        System.out.println("config 파일 저장 실패");
                    }
                }else{
                    System.out.println("압축 해제 실패");
                }
            }else{
                System.out.println(downLoadFile + " 없음");
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean reSignApk(String downloadPath, String apkName){
        try{
            System.out.println("Self-Signed Start");
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("jarsigner", "-verbose", "-keystore", downloadPath+"testKey3.jks", apkName, "key0", "-storepass", "1234qwer");
            builder.directory(new File(System.getProperty("user.home")));
            Process process = builder.start();
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
            System.out.println("Self-Signed Complete");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean reApkZip(String zipToolPath, String reApkFile, String zipUnzipPath){
	    try{
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(zipToolPath, "-1vr","../"+reApkFile, "*.*");
            builder.directory(new File(zipUnzipPath));
            Process process = builder.start();
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getConfigData(String ip,String port,String id,String pw){
	    String secretKey = "EJKFAS3JKFG3k23K";
        JSONObject jsonvar = new JSONObject();
        String result = null;
        try {
            jsonvar.put("SERVER_IP", ip);
            jsonvar.put("SERVER_PORT", port);
            jsonvar.put("ACCOUNT_ID", id);
            jsonvar.put("ACCOUNT_PW", pw);
            result = Utills.getInstance().AES_Encode(jsonvar.toJSONString(), secretKey);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }
        public void run() {
            try {
                new BufferedReader(new InputStreamReader(inputStream, "euc-kr")).lines()
                        .forEach(consumer);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }


}
