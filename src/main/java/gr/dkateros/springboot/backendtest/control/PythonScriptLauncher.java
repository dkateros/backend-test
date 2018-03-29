package gr.dkateros.springboot.backendtest.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Launches a python script. The script is bundled with the application in the resource
 * path {@value #SCRIPT_RESOURCE_PATH}. When this class is loaded this script is copied 
 * in {@link #TMP_DIR} folder of the system that runs the application as 'bt-script.py'
 */
@Component
public class PythonScriptLauncher {

	static final String TMP_DIR = System.getProperty("java.io.tmpdir") ;
	static final String PYTHON3_BINARY = "python3";
	static final String SCRIPT_RESOURCE_PATH = "/dumpdb.py";
	static final String SCRIPT_FS_PATH = TMP_DIR + File.separator + "bt-script.py";
	
	static {
		try {
			System.out.println("Will copy script to " + SCRIPT_FS_PATH);
			File target = new File(SCRIPT_FS_PATH);
			InputStream is = PythonScriptLauncher.class.getResourceAsStream(SCRIPT_RESOURCE_PATH);
			Files.copy(is, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		} 
	}
	
	@Value("${spring.datasource.username}") String dbUser;
	@Value("${spring.datasource.password}") String dbPass;
	@Value("${custom.db.host}") String dbHost;
	@Value("${custom.db.name}") String dbName;
	@Value("${custom.fs.csvpath}") String csvsPath; //CSV storage
	
	/**
	 * Executes the script stored in {@link #SCRIPT_FS_PATH} with python3. Also generates a file name,
	 * specified as a script argument. The script uses this to write the current DB data as CSV.
	 * 
	 * The process that runs the script has its standard out and standard error redirected to the standard
	 * out of the application.
	 * 
	 * @param id
	 *        Execution id. Used to generate a unique file name.
	 *         
	 * @return Waits for the process to complete and returns its return code.
	 */
	public int execute(int id) {
		String csvFile = csvsPath + File.separator + "dump-" + id + ".csv";
		ProcessBuilder processBuilder = new ProcessBuilder(PYTHON3_BINARY, SCRIPT_FS_PATH, dbUser, dbPass, dbHost, dbName, csvFile);
		try {
			Process process = processBuilder.start();
			redirect(process.getInputStream(), "sout-" + id + ": ");
			redirect(process.getErrorStream(), "serr-" + id + ": ");
			int returnCode = process.waitFor();
			System.out.println("script execution with id " + id + " exited with rc=" + returnCode);
			return returnCode;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Prints the contents of the specified stream to System.out stream. Each line is prefixed
	 * with the specified prefix.
	 * 
	 * @param inputStream
	 * 
	 * @param prefix
	 *        To distinguish standard out / err
	 * 
	 * @throws IOException
	 */
	void redirect(InputStream inputStream, String prefix) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		while((line = reader.readLine()) != null) {
			System.out.println(prefix + line);
		}
	}

}
