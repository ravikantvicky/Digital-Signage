package com.infosys.ds.controller;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.infosys.ds.exception.DSException;
import com.infosys.ds.model.Content;
import com.infosys.ds.model.FetchContentResponse;
import com.infosys.ds.service.DSClientService;
import com.infosys.ds.util.ContentUtils;

@Controller
@CrossOrigin
public class DSClientController {
	private Logger log = LoggerFactory.getLogger(DSClientController.class);
	@Autowired
	private DSClientService dsClientService;

	@GetMapping("/content/{contentId}")
	public void getContentBody(@PathVariable("contentId") int contentId, HttpServletResponse response) {
		String errorContent = null;
		try {
			Content content = dsClientService.fetchContentBody(contentId);
			try {
				byte[] data = null;
				String contentType = null;
				switch (content.getContentType().getContentTypeCd()) {
				case 1:
					data = content.getContentBody().getBytes();
					contentType = MimeTypeUtils.TEXT_HTML_VALUE;
					break;
				case 2:
					data = dsClientService.getPptContent(contentId).getBytes();
					contentType = MimeTypeUtils.TEXT_HTML_VALUE;
					break;
				case 4:
					String htmlContent = "<html>\r\n" + "<head>\r\n" + "<link rel=\"stylesheet\"\r\n"
							+ "	href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\"\r\n"
							+ "	integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\"\r\n"
							+ "	crossorigin=\"anonymous\">\r\n" + "</head>\r\n"
							+ "<body style=\"margin: 0; padding: 0\"><video class=\"embed-responsive\" autoplay videoPlayer loop muted playsinline style=\"width:"
							+ content.getWidth() + "px;height:" + content.getHeight() + "px\"><source type=\""
							+ content.getMimeType() + "\" src=\"data:" + content.getMimeType() + ";base64,"
							+ content.getContentBody()
							+ "\"></video><script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\"\r\n"
							+ "		integrity=\"sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN\"\r\n"
							+ "		crossorigin=\"anonymous\"></script>\r\n" + "	<script\r\n"
							+ "		src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js\"\r\n"
							+ "		integrity=\"sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q\"\r\n"
							+ "		crossorigin=\"anonymous\"></script>\r\n" + "	<script\r\n"
							+ "		src=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js\"\r\n"
							+ "		integrity=\"sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl\"\r\n"
							+ "		crossorigin=\"anonymous\"></script>\r\n" + "</body>\r\n" + "</html>";
					data = htmlContent.getBytes();
					contentType = MimeTypeUtils.TEXT_HTML_VALUE;
					break;
				case 5:
					data = content.getContentBody().getBytes();
					contentType = MimeTypeUtils.TEXT_PLAIN_VALUE;
					break;
				default:
					data = ContentUtils.base64Decode(content.getContentBody());
					if (!StringUtils.isEmpty(content.getMimeType()))
						contentType = content.getMimeType();
					else
						contentType = ContentUtils.getConetentType(data);
					if (contentType != null && (contentType.toLowerCase().startsWith("image")
							|| contentType.toLowerCase().contains("png") || contentType.toLowerCase().contains("jpg")))
						data = ContentUtils.resize(data, content.getWidth(), content.getHeight());
				}
				response.setContentType(contentType);
				OutputStream os = response.getOutputStream();
				os.write(data);
				os.flush();
			} catch (Exception e) {
				log.error("Error in writing response", e);
			}
		} catch (DSException e) {
			log.error("Error in fetching content body: {}", e.getMessage());
			errorContent = "<h2 style='color: red'>" + e.getMessage() + "</h2>";
		} catch (Exception e) {
			log.error("Error in fetching content body: {}", e);
			errorContent = "<h2 style='color: red'>Unable to fetch content !</h2>";
		}
		if (!StringUtils.isEmpty(errorContent)) {
			response.setContentType(MimeTypeUtils.TEXT_HTML_VALUE);
			try {
				OutputStream os = response.getOutputStream();
				os.write(errorContent.getBytes());
				os.flush();
			} catch (Exception e) {
				log.error("Error in writing response", e);
			}
		}
	}

	@GetMapping("/ppt/{contentId}/{slideNo}")
	public void showPptContent(@PathVariable("contentId") int contentId, @PathVariable("slideNo") int slideNo,
			HttpServletResponse response) {
		try {
			byte[] data = ContentUtils.base64Decode(dsClientService.getSlideContent(contentId, slideNo));
			response.setContentType(ContentUtils.getConetentType(data));
			OutputStream os = response.getOutputStream();
			os.write(data);
			os.flush();
		} catch (Exception e) {
			log.error("Error in writing response", e);
		}
	}

	@GetMapping("/fetchContent/{deviceId}")
	public @ResponseBody FetchContentResponse fetchContent(@PathVariable("deviceId") String deviceId) {

		try {
			return dsClientService.getContent();
		} catch (Exception e) {
			log.error("Error in writing response", e);
			return null;
		}
	}
}
