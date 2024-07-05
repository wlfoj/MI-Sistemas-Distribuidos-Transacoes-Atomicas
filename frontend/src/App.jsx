import Routes from "./utils/routes";

function App() {

	const bankCode = window._env_.BANK_CODE;

	const bankstruct = window._env_.BANK_STRUCT;
	
	localStorage.setItem('bankCode', bankCode);
	// console.log(localStorage.getItem('bankCode'))
	
	return (
		<>
			<Routes />
		</>
	);
}

export default App;
