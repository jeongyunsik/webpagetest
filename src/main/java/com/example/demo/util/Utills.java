package com.example.demo.util;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utills {
    private static Utills instance = null;

    private boolean isDownloadFile = false;
    private File downloadFile = null;

    private Utills(){

    }
    public static Utills getInstance(){
        if(instance == null) instance = new Utills();
        return instance;
    }

    public boolean compress(String path, String outputPath, String outputFileName) throws Throwable {
        // 파일 압축 성공 여부
        boolean isChk = false;
        File file = new File(path);
        // 파일의 .zip이 없는 경우, .zip 을 붙여준다.
        int pos = outputFileName.lastIndexOf(".") == -1 ? outputFileName.length() : outputFileName.lastIndexOf(".");
        // outputFileName .zip이 없는 경우
        if (!outputFileName.substring(pos).equalsIgnoreCase(".apk")) {
            outputFileName += ".apk";
        }
        // 압축 경로 체크
        if (!file.exists()) {
            throw new Exception("Not File!");
        }
        // 출력 스트림
        FileOutputStream fos = null;
        // 압축 스트림
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(new File(outputPath + outputFileName));
            zos = new ZipOutputStream(fos);
            // 디렉토리 검색를 통한 하위 파일과 폴더 검색
            searchDirectory(file, file.getPath(), zos);
            // 압축 성공.
            isChk = true;
        } catch (Throwable e) {
            throw e;
        } finally {
            if (zos != null) zos.close();
            if (fos != null) fos.close();
        }
        return isChk;
    }

    private void searchDirectory(File file, String root, ZipOutputStream zos) throws Exception {
        // 지정된 파일이 디렉토리인지 파일인지 검색
        if (file.isDirectory()) {
            // 디렉토리일 경우 재탐색(재귀)
            File[] files = file.listFiles();
            for (File f : files) {
//                System.out.println("file = > " + f);
                searchDirectory(f, root, zos);
            }
        } else {
            // 파일일 경우 압축을 한다.
            try {
                compressZip(file, root, zos);
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void compressZip(File file, String root, ZipOutputStream zos) throws Throwable {
        FileInputStream fis = null;
        try {
            String zipName = file.getPath().replace(root + "\\", "");
            // 파일을 읽어드림
            fis = new FileInputStream(file);
            // Zip엔트리 생성(한글 깨짐 버그)
            ZipEntry zipentry = new ZipEntry(zipName);
            // 스트림에 밀어넣기(자동 오픈)
            zos.putNextEntry(zipentry);
            int length = (int) file.length();
            byte[] buffer = new byte[length];
            // 스트림 읽어드리기
            fis.read(buffer, 0, length);
            // 스트림 작성
            zos.write(buffer, 0, length);
            // 스트림 닫기
            zos.closeEntry();
        } catch (Throwable e) {
            throw e;
        } finally {
            if (fis != null) fis.close();
        }
    }

    public boolean unZip(String zipPath, String zipFileName, String zipUnzipPath){
        boolean isChk = false;

        File zipFile = new File(zipPath + zipFileName);
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ZipEntry zipentry = null;

        try{

            if(makeFolder(zipUnzipPath)){
//                System.out.println("폴더를 생성했습니다");
            }
            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(fis, Charset.forName("EUC-KR"));

            while((zipentry = zis.getNextEntry()) != null){
                String filename = zipentry.getName();
//                System.out.println("file name >>> " + filename);
                File file = new File(zipUnzipPath, filename);

                if(zipentry.isDirectory()){
//                    System.out.println("zipentry가 디렉토리입니다.");
                    file.mkdirs();
                }else{
//                    System.out.println("zipentry가 파일입니다.");
                    try {
                        createZipFile(file,zis);
                    }catch (Throwable e){

                    }
                }
            }
            isChk = true;
        } catch (Exception e) {
            isChk = false;
        }finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException e) {

                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {

                }
            }
        }
        return isChk;
    }

    private boolean makeFolder(String folder){
        if(folder.length() < 0){
            return false;
        }
        String path = folder;
        File Folder = new File(path);

        if(!Folder.exists()){
            try{
                Folder.mkdir();
                System.out.println("폴더가 생성되었습니다.");
            }catch (Exception e){
                System.out.println("이미 폴더가 생성되어 있습니다.");
            }
        }else{

        }
        return true;
    }

    private boolean createParentFolder(File file){
        if(!file.exists()){
            if(!file.mkdir()){
                File parentDir = new File(file.getParent());
                createParentFolder(parentDir);
                file.mkdir();
            }
        }
        return true;
    }

    private void createZipFile(File file, ZipInputStream zis) throws Throwable{
        File parentDir = new File(file.getParent());
        createParentFolder(parentDir);
        FileOutputStream fos = null;

        try{
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            int size = 0;
            while((size = zis.read(buffer))>0){
                fos.write(buffer,0,size);
            }
        }catch (Throwable e){
            throw  e;
        }finally {
            if(fos != null){
                try{
                    fos.close();
                }catch (IOException e){

                }
            }
        }
    }

    public boolean deleteFolder(File file){
        File[] folder_list = file.listFiles(); //파일리스트 얻어오기
        for (int j = 0; j < folder_list.length; j++) {
            if(folder_list[j].isDirectory()){
                deleteFolder(folder_list[j]);
            }else{
//                System.out.println(folder_list[j] + "  파일 삭제되었습니다.");
                folder_list[j].delete(); //파일 삭제
            }
        }
        folder_list = file.listFiles(); //파일리스트 얻어오기
        if(folder_list.length == 0 && file.isDirectory()){
            file.delete(); //대상폴더 삭제
            System.out.println(file.getPath() + "  폴더가 삭제되었습니다.");
        }
        return file.exists();
    }

    public boolean createFile(String txt, File mFile){
        try{
            // true 지정시 파일의 기존 내용에 이어서 작성
            FileWriter fw = new FileWriter(mFile, false) ;
            // 파일안에 문자열 쓰기
            fw.write(txt);
            fw.flush();

            // 객체 닫기
            fw.close();

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Key getKeySpec(String key) throws UnsupportedEncodingException {
        String iv = key.substring(0, 16);
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        return keySpec;
    }

    public String AES_Encode(String str, String secretKey) throws NoSuchAlgorithmException,
            GeneralSecurityException, UnsupportedEncodingException{
        byte[] keyBytes = new byte[16];
        Key keySpec = getKeySpec(secretKey);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(keyBytes));
        byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
        String enStr = new String(Base64.encodeBase64(encrypted));
        System.out.println("enStr   " + enStr);
        return enStr;
    }

    public String AES_Decode(String str, String secretKey) throws NoSuchAlgorithmException,
            GeneralSecurityException, UnsupportedEncodingException{
        byte[] keyBytes = new byte[16];
        Key keySpec = getKeySpec(secretKey);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(keyBytes));
        byte[] byteStr = Base64.decodeBase64(str.getBytes());
        return new String(c.doFinal(byteStr), "UTF-8");
    }

    public boolean isDownloadFile() {
        return isDownloadFile;
    }

    public void setDownloadFile(boolean downloadFile) {
        isDownloadFile = downloadFile;
    }
    public File getDownloadFile(){
        return downloadFile;
    }
    public void setDownloadFile(File f){
        downloadFile = f;
    }
}
