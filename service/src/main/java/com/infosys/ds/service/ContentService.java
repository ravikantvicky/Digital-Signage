package com.infosys.ds.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.infosys.ds.exception.DSException;
import com.infosys.ds.model.Content;
import com.infosys.ds.model.ContentDimenssion;
import com.infosys.ds.model.ContentType;
import com.infosys.ds.repository.ContentRepository;
import com.infosys.ds.util.ContentUtils;

@Service
public class ContentService {
	private Logger log = LoggerFactory.getLogger(ContentService.class);
	@Autowired
	private ContentRepository contentRepository;
	@Autowired
	private Environment env;

	public String saveContent(ContentDimenssion content) throws DSException {
		try {
			StringBuilder htmlData = new StringBuilder();
			Content data = new Content();
			data.setContentBody(saveContentList(content.getChildArr()));
			data.setContentType(new ContentType());
			data.getContentType().setContentTypeCd(1);
			data.setHeight((int) content.getHeight());
			data.setWidth((int) content.getWidth());
			data.setMimeType("text/html");
			data = saveContentBody(data);
			htmlData.append("<html>\r\n" + "<head>\r\n" + "<link rel=\"stylesheet\"\r\n"
					+ "	href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\"\r\n"
					+ "	integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\"\r\n"
					+ "	crossorigin=\"anonymous\">\r\n" + "</head>\r\n" + "<body style=\"margin: 0; padding: 0\">");
			htmlData.append("<iframe width=\"");
			htmlData.append(content.getWidth());
			htmlData.append("\" height=\"");
			htmlData.append(content.getHeight());
			htmlData.append("\" src=\"");
			htmlData.append(env.getProperty("url.content") + data.getContentId());
			htmlData.append("\" style=\"overflow:hidden;border:none\" />");
			htmlData.append("<script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\"\r\n"
					+ "		integrity=\"sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN\"\r\n"
					+ "		crossorigin=\"anonymous\"></script>\r\n" + "	<script\r\n"
					+ "		src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js\"\r\n"
					+ "		integrity=\"sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q\"\r\n"
					+ "		crossorigin=\"anonymous\"></script>\r\n" + "	<script\r\n"
					+ "		src=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js\"\r\n"
					+ "		integrity=\"sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl\"\r\n"
					+ "		crossorigin=\"anonymous\"></script>\r\n" + "</body>\r\n" + "</html>");

			return htmlData.toString();
		} catch (DSException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in saving content", e);
			throw new DSException("Unable to save content !");
		}
	}

	private Content saveContentBody(Content content) throws DSException {
		try {
			if (content.getContentType().getContentTypeCd() == 2) {
				content.setContentId(contentRepository.saveContent(null, 2, content.getHeight(), content.getWidth(), 1,
						content.getMimeType()));
				contentRepository.saveSlides(content.getContentId(), ContentUtils.ppt2Png(content.getContentBody()));
			} else {
				content.setContentId(contentRepository.saveContent(content.getContentBody(),
						content.getContentType().getContentTypeCd(), content.getHeight(), content.getWidth(), 1,
						content.getMimeType()));
			}
			return content;
		} catch (DSException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in saving content", e);
			throw new DSException("Unable to save content !");
		}
	}

	private String saveContentList(List<ContentDimenssion> data) throws DSException {
		try {
			StringBuilder htmlData = new StringBuilder();
			htmlData.append("<div style=\"position:absolute;\">");
			for (ContentDimenssion content : data)
				htmlData.append(saveContentData(content));
			htmlData.append("</div>");
			return htmlData.toString();
		} catch (DSException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in saving content data", e);
			throw new DSException("Unable to save content !");
		}
	}

	private String saveContentData(ContentDimenssion content) throws DSException {
		if (content == null)
			return "";
		try {
			if (content.getChildArr() != null && !content.getChildArr().isEmpty())
				return saveContentList(content.getChildArr());
			Content contentBody = new Content();
			contentBody.setContentBody(content.getBase64Url());
			contentBody.setContentType(new ContentType());
			contentBody.getContentType().setContentTypeCd(getContentTypeCode(content.getType()));
			contentBody.setHeight((int) content.getHeight());
			contentBody.setWidth((int) content.getWidth());
			contentBody.setMimeType(content.getType());
			contentBody = saveContentBody(contentBody);
			StringBuilder htmlData = new StringBuilder();
			htmlData.append("<div style=\"position:absolute;top:");
			htmlData.append(content.getStartY() + "px");
			htmlData.append(";left:");
			htmlData.append(content.getStartX() + "px");
			htmlData.append(";right:");
			htmlData.append((content.getStartX() + content.getWidth()) + "px");
			htmlData.append(";bottom:");
			htmlData.append((content.getStartY() + content.getHeight()) + "px");
			htmlData.append(";\">");
			htmlData.append("<iframe width=\"");
			htmlData.append(content.getWidth());
			htmlData.append("\" height=\"");
			htmlData.append(content.getHeight());
			htmlData.append("\" src=\"");
			htmlData.append(env.getProperty("url.content") + contentBody.getContentId());
			htmlData.append("\" style=\"overflow:hidden;border:none\" ></iframe></div>");
			return htmlData.toString();
		} catch (DSException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in saving content data", e);
			throw new DSException("Unable to save content !");
		}
	}

	private int getContentTypeCode(String contentTypeName) throws DSException {
		try {
			if (contentTypeName.equalsIgnoreCase("text/html"))
				return 1;
			if (contentTypeName
					.equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
				return 2;
			if (contentTypeName.toLowerCase().startsWith("image"))
				return 3;
			if (contentTypeName.toLowerCase().startsWith("video"))
				return 4;
			if (contentTypeName.equalsIgnoreCase("text/plain"))
				return 5;
			else
				throw new DSException("Content type " + contentTypeName + " not supported !");
		} catch (DSException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error in saving content data", e);
			throw new DSException("Unable to save content !");
		}
	}
}
