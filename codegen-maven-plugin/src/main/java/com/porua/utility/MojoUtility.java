package com.porua.utility;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

@SuppressWarnings("unchecked")
public class MojoUtility {

	private static ClassLoader loader = null;

	public static ClassLoader getClassLoader(MavenProject mavenProject, RepositorySystem repoSystem,
			RepositorySystemSession session, List<RemoteRepository> remoteRepositories) throws Exception {

		if (MojoUtility.loader == null) {
			List<URL> listDependency = getProjectDependencies(mavenProject, repoSystem, session, remoteRepositories);
			MojoUtility.loader = new URLClassLoader(listDependency.toArray(new URL[listDependency.size()]), Thread.currentThread().getContextClassLoader());
		}
		return MojoUtility.loader;
	}

	/**
	 * Get dependent artifact jars.
	 * {@link https://vzurczak.wordpress.com/2016/01/08/finding-dependencies-artifacts-in-your-maven-plug-in/}
	 * 
	 * @param mavenProject
	 * @param repoSystem
	 * @param session
	 * @param remoteRepositories
	 * @return
	 * @throws Exception
	 */
	public static List<URL> getProjectDependencies(MavenProject mavenProject, RepositorySystem repoSystem,
			RepositorySystemSession session, List<RemoteRepository> remoteRepositories) throws Exception {
		List<URL> list = new ArrayList<>();

		// Get the dependent artifacts.
		Set<org.apache.maven.artifact.Artifact> set = mavenProject.getDependencyArtifacts();
		set.add(mavenProject.getArtifact());

		// Get the URL of each artifact.
		Iterator<org.apache.maven.artifact.Artifact> itr = mavenProject.getDependencyArtifacts().iterator();
		while (itr.hasNext()) {
			org.apache.maven.artifact.Artifact unresolvedArtifact = itr.next();

			org.eclipse.aether.artifact.Artifact aetherArtifact = new DefaultArtifact(unresolvedArtifact.getGroupId(), unresolvedArtifact.getArtifactId(), unresolvedArtifact.getClassifier(), unresolvedArtifact.getType(), unresolvedArtifact.getVersion());
			ArtifactRequest request = new ArtifactRequest().setArtifact(aetherArtifact).setRepositories(remoteRepositories);
			ArtifactResult result = repoSystem.resolveArtifact(session, request);
			list.add(result.getArtifact().getFile().toURI().toURL());

		}
		return list;
	}
}
