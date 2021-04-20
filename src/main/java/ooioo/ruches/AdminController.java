package ooioo.ruches;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

import ooioo.ruches.personne.PersonneService;

@Controller
public class AdminController {
	
	private final Logger logger = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
	private PersonneService personneService;

	@Value("${accueil.titre}")
	private String accueilTitre;
	@Value("${dump.path}")
	private String dumpPath;
	@Value("${git.url}")
	private String gitUrl;
	@Value("${tomcat.webapps.path}")
	private String tomcatWebappsPath;
	
	/*
	 * Download des dumps de la base
	 * fichiers ruches_schema.sql et ruches.sql.xz
	 */
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
	
	/*
	 * Déploiement de l'application
	 * git clone, maven package et copie du war sous webapps
	 */
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
			Files.copy(sourcePath, destinationPath,
			            StandardCopyOption.REPLACE_EXISTING);
			destinationPath = Paths.get(tomcatWebappsPath + "ruchestest.war");
			Files.copy(sourcePath, destinationPath,
			            StandardCopyOption.REPLACE_EXISTING);
			/*
			destinationPath = Paths.get(tomcatWebappsPath + "ruchesmarion.war");
			Files.copy(sourcePath, destinationPath,
			            StandardCopyOption.REPLACE_EXISTING);
			*/
		} catch (IOException|InterruptedException e) {
			e.printStackTrace();
		}
		// redirect pour réécriture de l'url sans /deploie
		// sinon F5 relance le déploiement
		// model.addAttribute(Const.MESSAGE, "Application mise à jour, attendre quelques minutes son redémarrage.");
		//  model n'est pas conservé après un redirect
		return "redirect:/";
	}
	
}