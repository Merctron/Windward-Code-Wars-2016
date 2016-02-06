package net.windward.Acquire.AI;

import java.util.ArrayList;
import java.util.List;

public enum Company {
	WINDWARD("Windward", ProfitPotential.HIGH), HP("Hewlett-Packard", ProfitPotential.HIGH), JETBRAINS("JetBrains", ProfitPotential.MID), MICROSOFT("Microsoft", ProfitPotential.MID), SALESFORCE("salesforce.com", ProfitPotential.MID), AMAZON("Amazon", ProfitPotential.LOW), GOOGLE("Google", ProfitPotential.LOW);
	
	private final ProfitPotential profitPotential;
	private final String codeName;
	
	private Company(String codeName, ProfitPotential profitPotential) {
		this.codeName = codeName;
		this.profitPotential = profitPotential;
	}
	
	public String getCodeName() {
		return codeName;
	}
	
	public static List<Company> getCompaniesWithPotential(ProfitPotential potential) {
		List<Company> companies = new ArrayList<Company>();
		for(Company company: values()) {
			if( company.getProfitPotential() == potential ) {
				companies.add(company);
			}
		}
		return companies;
	}

	public ProfitPotential getProfitPotential() {
		return profitPotential;
	}
	
	public static enum ProfitPotential {
		LOW, MID, HIGH;
	}
}
