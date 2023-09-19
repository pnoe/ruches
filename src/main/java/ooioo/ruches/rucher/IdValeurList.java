package ooioo.ruches.rucher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import ooioo.ruches.Const;

public class IdValeurList {

	private List<IdValeur> idValLst;

	@DateTimeFormat(pattern = Const.YYYYMMDDHHMM)
	private LocalDateTime date;

	private String commentaire;

	public IdValeurList() {
		this.idValLst = new ArrayList<>();
	}

	public List<IdValeur> getIdValLst() {
		return idValLst;
	}

	public void setIdValLst(List<IdValeur> idValLst) {
		this.idValLst = idValLst;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

}