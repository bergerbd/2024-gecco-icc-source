module de.evoal.core.si {
	types:
		cardinal type Newton description : "SI-Unit for force, measure in Newton[N=kg*m/s*s]."
			with constraints:
				value >= 0.0;
		;
		
		quotient type Meter description: "SI-Unit for lengths, measured in metres[m]."
			with constraints: 
				value >= 0.0;
		;
		
		quotient type Second description: "SI-Unit for time, measured in seconds[s]."
			with constraints: 
				value >= 0.0;
		;
		
		quotient type Kilogram description: "SI-Unit for mass, measured in kilogram[kg]."
			with constraints: 
				value >= 0.0;
		;
		
		quotient type Ampere description: "SI-Unit for eletric current, measured in ampere[A]."
			with constraints: 
				value >= 0.0;
		;
		
		quotient type Kelvin description: "SI-Unit for thermodynamic temperature, measured in kelvin[K]."
			with constraints:
				value >= 0.0;
		;
		
		quotient type Mole description: "SI-Unit for amount of substance, measure in mole[mol]."
			with constraints:
				value >= 0.0;
		;
		
		quotient type Candela description: "SI-Unit for luminous intensity, measured in candela[cd]."
			with constraints:
				value >= 0.0;
		;
		
		quotient type Hertz description: "SI-Unit for frequency, measured in hertz[Hz=1/s]."
			with constraints: 
				value >= 0.0;
		;
		
		quotient type Speed description: "SI-Unit for frequency, measured in metre per second[m/s]."
			with constraints:
				value >= 0.0;
		;
		
		quotient type Pascal description: "SI-Unit for pressure, measure in Pascal[Pa=kg/m*s*s]."
			with constraints:
				value >= 0.0;
		;
		
		cardinal type Celsius description: "SI-Unit for temperature relative to 273.15 K, measured in Celsius[°C=K]"
			with constraints:
				value >= -273.15; 
		;
		
		quotient type Area description: "SI-Unit for areas, measured in square meter[m*m]"
			with constraints:
				value >= 0.0;
		;
}	