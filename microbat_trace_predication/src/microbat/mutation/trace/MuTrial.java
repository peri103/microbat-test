package microbat.mutation.trace;

import tregression.empiricalstudy.EmpiricalTrial;

public class MuTrial {
	private EmpiricalTrial trial;
	private String orgFilePath;
	private String mutationFilePath;

	public MuTrial(EmpiricalTrial trial, String orgFilePath, String mutationFilePath) {
		this.trial = trial;
		this.orgFilePath = orgFilePath;
		this.mutationFilePath = mutationFilePath;
	}

	public EmpiricalTrial getTrial() {
		return trial;
	}

	public void setTrial(EmpiricalTrial trial) {
		this.trial = trial;
	}

	public String getOrgFilePath() {
		return orgFilePath;
	}

	public void setOrgFilePath(String orgFilePath) {
		this.orgFilePath = orgFilePath;
	}

	public String getMutationFilePath() {
		return mutationFilePath;
	}

	public void setMutationFilePath(String mutationFilePath) {
		this.mutationFilePath = mutationFilePath;
	}

}