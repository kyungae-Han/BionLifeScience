package com.dev.BionLifeScienceWeb.service.namecard;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 직원이 입력한 주소로 QR 을 그때그때 만든다. 그림 파일을 올려 두지 않으므로
 * 주소만 바꾸면 QR 이 따라 바뀐다.
 *
 * 결과는 SVG 다. 화면 크기와 무관하게 또렷하고, 이미지 파일을 저장할 필요가 없다.
 */
@Service
public class NameCardQrService {

	/** QR 둘레의 여백. 규격상 4모듈을 비워야 인식률이 떨어지지 않는다 */
	private static final int QUIET_ZONE = 4;

	/** 명함 QR 은 인쇄물이 아니라 화면이라 M(15%) 이면 충분하다 */
	private static final ErrorCorrectionLevel ERROR_CORRECTION = ErrorCorrectionLevel.M;

	public String toSvg(String text) {
		BitMatrix matrix = encode(text);
		int size = matrix.getWidth();
		int canvas = size + QUIET_ZONE * 2;

		StringBuilder sb = new StringBuilder(1024);
		sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
		  .append(canvas).append(' ').append(canvas)
		  .append("\" shape-rendering=\"crispEdges\" role=\"img\">");
		sb.append("<rect width=\"").append(canvas).append("\" height=\"").append(canvas)
		  .append("\" fill=\"#ffffff\"/>");

		// 검은 모듈이 가로로 이어지면 한 덩어리로 묶는다. 파일이 서너 배 작아진다.
		for (int y = 0; y < size; y++) {
			int runStart = -1;
			for (int x = 0; x <= size; x++) {
				boolean dark = x < size && matrix.get(x, y);
				if (dark && runStart < 0) {
					runStart = x;
				} else if (!dark && runStart >= 0) {
					sb.append("<rect x=\"").append(runStart + QUIET_ZONE)
					  .append("\" y=\"").append(y + QUIET_ZONE)
					  .append("\" width=\"").append(x - runStart)
					  .append("\" height=\"1\" fill=\"#0f172a\"/>");
					runStart = -1;
				}
			}
		}
		sb.append("</svg>");
		return sb.toString();
	}

	private BitMatrix encode(String text) {
		Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.ERROR_CORRECTION, ERROR_CORRECTION);
		// 여백은 SVG 를 그리면서 직접 넣는다. 여기서 또 넣으면 두 번 들어간다.
		hints.put(EncodeHintType.MARGIN, 0);
		try {
			// 폭·높이를 모듈 수와 맞춰 1모듈 = 1단위로 받는다. 크기는 SVG 의 viewBox 가 정한다.
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix probe = writer.encode(text, com.google.zxing.BarcodeFormat.QR_CODE, 1, 1, hints);
			return probe;
		} catch (WriterException e) {
			throw new IllegalArgumentException("QR 로 만들 수 없는 주소입니다: " + text, e);
		}
	}
}
