package com.yunhuakeji.attendance.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * 二维码生成和读的工具类
 */
public class QRCodeUtil {

  /**
   * 生成包含字符串信息的二维码图片
   *
   * @param outputStream 文件输出流路径
   * @param content      二维码携带信息
   * @param qrCodeSize   二维码图片大小
   * @param imageFormat  二维码的格式
   * @throws WriterException
   * @throws IOException
   */
  public static boolean createQrCode(OutputStream outputStream, String content, int qrCodeSize, String imageFormat) throws WriterException, IOException {
    //设置二维码纠错级别ＭＡＰ
    Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  // 矫错级别
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    //创建比特矩阵(位矩阵)的QR码编码的字符串
    BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
    // 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
    int matrixWidth = byteMatrix.getWidth();
    BufferedImage image = new BufferedImage(matrixWidth - 200, matrixWidth - 200, BufferedImage.TYPE_INT_RGB);
    image.createGraphics();
    Graphics2D graphics = (Graphics2D) image.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, matrixWidth, matrixWidth);
    // 使用比特矩阵画并保存图像
    graphics.setColor(Color.BLACK);
    for (int i = 0; i < matrixWidth; i++) {
      for (int j = 0; j < matrixWidth; j++) {
        if (byteMatrix.get(i, j)) {
          graphics.fillRect(i - 100, j - 100, 1, 1);
        }
      }
    }
    return ImageIO.write(image, imageFormat, outputStream);
  }

  /**
   * 生成包含字符串信息的二维码图片
   *
   * @param content     二维码携带信息
   * @param qrCodeSize  二维码图片大小
   * @throws WriterException
   * @throws IOException
   */
  public static BufferedImage createQrCode(String content, int qrCodeSize) throws WriterException, IOException {
    //设置二维码纠错级别ＭＡＰ
    Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);  // 矫错级别
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    //创建比特矩阵(位矩阵)的QR码编码的字符串
    BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
    // 使BufferedImage勾画QRCode  (matrixWidth 是行二维码像素点)
    int matrixWidth = byteMatrix.getWidth();
    BufferedImage image = new BufferedImage(matrixWidth - 200, matrixWidth - 200, BufferedImage.TYPE_INT_RGB);
    image.createGraphics();
    Graphics2D graphics = (Graphics2D) image.getGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, matrixWidth, matrixWidth);
    // 使用比特矩阵画并保存图像
    graphics.setColor(Color.BLACK);
    for (int i = 0; i < matrixWidth; i++) {
      for (int j = 0; j < matrixWidth; j++) {
        if (byteMatrix.get(i, j)) {
          graphics.fillRect(i - 100, j - 100, 1, 1);
        }
      }
    }
    return image;
  }


  /**
   * 测试代码
   *
   * @throws WriterException
   */
  public static void main(String[] args) throws IOException, WriterException {

    createQrCode(new FileOutputStream(new File("d:\\qrcode.jpg")), "WE1231238239128sASDASDSADSDWEWWREWRERWSDFDFSDSDF123123123123213123", 900, "JPEG");

  }
}

