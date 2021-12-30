package ooioo.ruches;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Controller
public class AdminController {

	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;

	@Value("${accueil.titre}")
	private String accueilTitre;
	@Value("${dump.path}")
	private String dumpPath;
	@Value("${git.url}")
	private String gitUrl;
	@Value("${tomcat.webapps.path}")
	private String tomcatWebappsPath;
	@Value("${tomcat.oldwebapps.path}")
	private String tomcatOldWebappsPath;

	/**
	 * Tests erreurs
	 *  résultat dans la page tests.html
	 */
	@GetMapping("/tests")
	public String tests(Model model) {
		// Recherche des erreurs dans l'historique des ajouts de ruches dans les ruchers
		Iterable<Rucher> ruchers = rucherRepository.findAll();
		// la liste de tous les événements RUCHEAJOUTRUCHER triés par ordre de date descendante
		List<Evenement> evensListe = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHEAJOUTRUCHER);
		int levens = evensListe.size();
		StringBuilder histoLog = new StringBuilder();
		Formatter histoFormat = new Formatter(histoLog);
		for (Rucher rucher : ruchers) {
			Long rucherId = rucher.getId();
			// Les nom des ruches présentes dans le rucher
			Collection<Nom> nomRuchesX = rucheRepository.findNomsByRucherId(rucherId);
 			List<String> ruches = new ArrayList<>();
 			for (Nom nomR : nomRuchesX) {
 				ruches.add(nomR.getNom());
 			}
			for (int i = 0; i < levens; i++) {
				Evenement eve = evensListe.get(i);
				if ((eve.getRucher() == null) || (eve.getRuche() == null)) {
					// si événement incorrect, on l'ignore
					continue;
				}
 				if (eve.getRucher().getId().equals(rucherId)) {
					// si l'événement est un ajout dans le rucher
					// on retire la ruche de l'événement 
					//  de la liste des ruches du rucher
					if (!ruches.remove(eve.getRuche().getNom())) {
						histoFormat.format("Événement %s le rucher %s ne contient pas la ruche %s <br/>", 
								eve.getDate(), eve.getRucher().getNom(), eve.getRuche().getNom());
					}
				} else {
					// l'événenemt eve ajoute une ruche dans un autre rucher
					
					// On cherche l'événement précédent ajout de cette ruche
					Evenement evePrec = null;
					for (int j = i + 1; j < levens; j++) {
						if ((evensListe.get(j).getType() == 
								ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER) &&
								(evensListe.get(j).getRuche() != null) &&
							(evensListe.get(j).getRuche().getId().equals(eve.getRuche().getId()))) {
							evePrec = evensListe.get(j);
							break;
						}
					}
					if (evePrec != null) {
						//   si c'est un ajout dans le rucher rucherId
						if (evePrec.getRucher() == null) {
							continue;
						}
						if (evePrec.getRucher().getId().equals(rucherId)) {
							// si l'événement précédent evePrec était un ajout dans le
							//   rucher, alors eve retire la ruche du rucher
							ruches.add(eve.getRuche().getNom());
						}
					}
				}
			}
			if (!ruches.isEmpty()) {
				histoFormat.format("Après traitement des événements, le rucher %s n'est pas vide<br/>",
						rucher.getNom());
			} 			
		}
		histoFormat.close();
		model.addAttribute("histoLog", histoLog);
		// evenInc événements incorrects
		StringBuilder evenInc = new StringBuilder();
		Formatter evenIncFormat = new Formatter(evenInc);
		for (Evenement eve : evensListe) {
			if ((eve.getRucher() == null) || (eve.getRuche() == null)) {
				evenIncFormat.format("Événement RUCHEAJOUTRUCHER %s incomplet. Ruche %s Rucher %s<br/>",
						eve.getDate(), eve.getRuche() == null ? "Null" : eve.getRuche().getNom(),
								eve.getRucher() == null ? "Null" : eve.getRucher().getNom());
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.HAUSSEPOSERUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || eve.getRuche() == null) {
				evenIncFormat.format("Événement HAUSSEPOSERUCHE %s incomplet. Hausse %s Ruche %s<br/>",
						eve.getDate(), eve.getHausse() == null ? "null" : 
							eve.getHausse().getNom(),eve.getRuche());
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.HAUSSERETRAITRUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || eve.getRuche() == null) {
				evenIncFormat.format("Événement HAUSSERETRAITRUCHE %s incomplet. Hausse %s Ruche %s<br/>",
						eve.getDate(), eve.getHausse() == null ? "null" :
							eve.getHausse().getNom(), eve.getRuche());
			}
		}

		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.AJOUTESSAIMRUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null || eve.getRuche() == null) {
				evenIncFormat.format("Événement AJOUTESSAIMRUCHE %s incomplet. Essaim %s Ruche %s<br/>",
						eve.getDate(), eve.getEssaim() == null ? "null" :
							eve.getEssaim().getNom(), eve.getRuche());
			}
		}

		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.HAUSSEREMPLISSAGE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null || !isPourCent(eve.getValeur())) {
				evenIncFormat.format("Événement HAUSSEREMPLISSAGE %s incorrect. Hausse %s Valeur %s<br/>",
						eve.getDate(), eve.getHausse() == null ? "null" :
							eve.getHausse().getNom(), eve.getValeur());
			}
		}
		
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.ESSAIMTRAITEMENT);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				evenIncFormat.format("Événement ESSAIMTRAITEMENT %s incomplet. Essaim %s<br/>",
						eve.getDate(), eve.getEssaim() == null ? "null" :
							eve.getEssaim().getNom());
			}
		}
		
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.ESSAIMTRAITEMENTFIN);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				evenIncFormat.format("Événement ESSAIMTRAITEMENTFIN %s incomplet. Essaim %s<br/>",
						eve.getDate(), eve.getEssaim() == null ? "null" :
							eve.getEssaim().getNom());
			}
		}
		
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.ESSAIMSUCRE);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null || !isNum(eve.getValeur())) {
				evenIncFormat.format("Événement ESSAIMSUCRE %s incorrect. Essaim %s Valeur %s<br/>",
						eve.getDate(), eve.getEssaim() == null ? "null" :
							eve.getEssaim().getNom(),
					eve.getValeur());
			}
		}
		
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.COMMENTAIRERUCHE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null) {
				evenIncFormat.format("Événement COMMENTAIRERUCHE %s incomplet. Ruche %s<br/>",
						eve.getDate(), "null");
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.COMMENTAIREHAUSSE);
		for (Evenement eve : evensListe) {
			if (eve.getHausse() == null) {
				evenIncFormat.format("Événement COMMENTAIREHAUSSE %s incomplet. Hausse %s<br/>",
						eve.getDate(), "null");
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.COMMENTAIREESSAIM);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				evenIncFormat.format("Événement COMMENTAIREESSAIM %s incomplet. Essaim %s<br/>",
						eve.getDate(), "null");
			}
		}
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.COMMENTAIRERUCHER);
		for (Evenement eve : evensListe) {
			if (eve.getRucher() == null) {
				evenIncFormat.format("Événement COMMENTAIRERUCHER %s incomplet. Rucher %s<br/>",
						eve.getDate(), "null");
			}
		}
		
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.ESSAIMDISPERSION);
		for (Evenement eve : evensListe) {
			if (eve.getEssaim() == null) {
				evenIncFormat.format("Événement ESSAIMDISPERSION %s incomplet. Essaim %s<br/>",
						eve.getDate(), "null");
			}
		}
		
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.RUCHEPESEE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null || eve.getEssaim() == null || !isNum(eve.getValeur())) {
				evenIncFormat.format("Événement RUCHEPESEE %s incomplet. Ruche %s Essaim %s Valeur %s<br/>",
						eve.getDate(),
						eve.getRuche() == null ? "null" :
							eve.getRuche().getNom(),
						eve.getEssaim() == null ? "null" :
								eve.getEssaim().getNom(),
						eve.getValeur());
			}
		}
		
		evensListe = evenementRepository.findByTypeOrderByDateDesc(
				TypeEvenement.RUCHECADRE);
		for (Evenement eve : evensListe) {
			if (eve.getRuche() == null) {
				evenIncFormat.format("Événement RUCHECADRE %s incorrect. Ruche %s<br/>",
						eve.getDate(), "null");
			}
		}
		
		evenIncFormat.close();
		model.addAttribute("evenInc", evenInc);
		return "tests";
	}

	public static boolean isPourCent(String str) {
	    if (str == null) {
	        return false;
	    }
	    try {
	        int d = Integer.parseInt(str);
	        if (d < 0 || d > 100) {
		    	return false;
		    }
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	public static boolean isNum(String str) {
	    if (str == null) {
	        return false;
	    }
	    try {
	    	Double.parseDouble(str);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	/*
	 * Download des dumps de la base
	 * fichiers ruches_schema.sql et ruches.sql.xz
	 *  ces fichiers dump sont créés par un cron sur le serveur
	 */
	/*
	@GetMapping(path = "/dump/{fichier}")
	@ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Resource> download(Model model, @PathVariable String fichier, Authentication authentication) throws IOException {
		ByteArrayResource resource = null;
		long fileLength = 0l;
		if (personneService.personneAdmin(authentication, model)) {
			File file = new File(dumpPath + fichier);
			Path path = Paths.get(file.getAbsolutePath());
	        resource = new ByteArrayResource(Files.readAllBytes(path));
	        fileLength = file.length();
		}
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fichier);
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(fileLength)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
	*/
	
	/*
	 * Déploiement de l'application
	 * git clone, maven package et copie du war sous webapps
	 */
	/*
	@GetMapping(path = "/deploie")
	public String deploie(Model model, Authentication authentication) {
		if (!personneService.personneAdmin(authentication, model)) {
			return Const.INDEX;
		}
		if (gitUrl.contentEquals("")) {
			model.addAttribute(Const.MESSAGE, "Déploiement de l'application non disponible.");
			return Const.INDEX;
		}
		model.addAttribute(Const.ACCUEILTITRE, accueilTitre);
		ProcessBuilder processBuilder = new ProcessBuilder();
		String tempDir = "/tmp";
		// System.getProperty("java.io.tmpdir"); donne le répertoire temp de tomcat
		processBuilder.directory(new File(tempDir));
		processBuilder.redirectErrorStream(true);
		FileSystemUtils.deleteRecursively(new File(tempDir + "/ruches"));
		try {
			processBuilder.command("bash", "-c", "git clone " + gitUrl);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			StringBuilder output = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line + " ");
			}
			int exitVal = process.waitFor();
			if (exitVal != 0) {
				logger.error(output.toString());
				model.addAttribute(Const.MESSAGE, "Erreur git clone : " + output);
					//	messageSource.getMessage(Const.ERREURGITCLONE, null, LocaleContextHolder.getLocale()));
				return Const.INDEX;
			}
			// Création du war avec maven
			processBuilder.directory(new File(tempDir + "/ruches"));
			processBuilder.command("bash", "-c", "mvn clean package -DskipTests");
			process = processBuilder.start();
			reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			output = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			exitVal = process.waitFor();
			if (exitVal != 0) {
				logger.error(output.toString());
				model.addAttribute(Const.MESSAGE, "Erreur maven package : " + output);
				//	messageSource.getMessage(Const.ERREURMAVENPACKAGE, null, LocaleContextHolder.getLocale()));
				return Const.INDEX;
			}
			// Copie du war dans le répertoire webapps de tomcat pour autodéploiement
			// pas de version dans le nom du war, voir finalname dans pom.xml
			Path sourcePath      = Paths.get(tempDir + "/ruches/target/ruches.war.original");
			Path destinationPath = Paths.get(tomcatWebappsPath + "ruches.war");
			Files.copy(destinationPath, destinationPath,
			            StandardCopyOption.REPLACE_EXISTING);
			destinationPath = Paths.get(tomcatWebappsPath + "ruchestest.war");
			Files.copy(sourcePath, destinationPath,
			            StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException|InterruptedException e) {
			e.printStackTrace();
		}
		// redirect pour réécriture de l'url sans /deploie
		// sinon F5 relance le déploiement
		// model.addAttribute(Const.MESSAGE, "Application mise à jour, attendre quelques minutes son redémarrage.");
		//  model n'est pas conservé après un redirect
		return "redirect:/";
	}
	*/

}