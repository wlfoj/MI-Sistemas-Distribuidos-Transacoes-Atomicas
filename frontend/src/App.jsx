import Routes from "./utils/routes";

function App() {

	const bankCode = window._env_ ? window._env_.BANK_CODE : "1";
	const bankstruct = window._env_ ? window._env_.BANK_STRUCT : "17";
	localStorage.setItem('bankCode', bankCode);
	console.log(bankstruct)
	// console.log(localStorage.getItem('bankCode'))
	
	return (
		<>
			<Routes />
		</>
	);
}

export default App;
