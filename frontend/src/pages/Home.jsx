import { useHistory } from "react-router-dom";
import { Container, Row, Col, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';


const container = {
	backgroundColor: '#FFFFFF', padding: '2rem', marginTop: '9rem',
	boxShadow: '4px 4px 4px lightgray', borderRadius: '6px'
}

const coluna = {
	backgroundColor: '#FFFFFF', margin: '0.5rem', alignItems: 'center'
}

const botao = {
	backgroundColor: '#5E3797', border: '0', marginLeft: '1rem', width: '80%', fontSize: '1.5rem'
}

const logout = {
	backgroundColor: '#5E3797', border: '0'
}

export default function Home() {

	const history = useHistory();

	const Logout = () => {
		localStorage.clear(); // Limpa o localStorage
		const bankCode = window._env_ ? window._env_.BANK_CODE : null;
		localStorage.setItem('bankCode', bankCode);
		history.replace("/login"); //Redireciona para tela de Login
	};

	return (
		<>
			<Container style={container}>
				<Row>
					<Col style={{ textAlign: 'right' }}>
						<Button style={logout} onClick={Logout}> Logout </Button>
					</Col>
				</Row>
				<Row className="justify-content-md-center" style={{ padding: '1rem', textAlign: 'center' }}>
					<h1 className="display-6"> Serviços </h1>
				</Row>
				<Row className="justify-content-md-center" style={{ paddingBottom: '1rem', paddingTop: '1rem' }}>
					<Col md style={coluna}>
						<Link to="/payment">
							<Button style={botao}> Pagamentos </Button>
						</Link>
					</Col>
					<Col md style={coluna}>
						<Link to="/deposit">
							<Button style={botao}> Depósitos </Button>
						</Link>
					</Col>
					<Col md style={coluna}>
						<Link to="/transfer">
							<Button style={botao}> Transferência </Button>
						</Link>
					</Col>
				</Row>
			</Container>
		</>
	);
}