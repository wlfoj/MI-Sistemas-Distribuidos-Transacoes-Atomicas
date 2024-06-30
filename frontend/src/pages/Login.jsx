import { useState } from 'react';
import { useHistory } from "react-router-dom";
import { Container, Row, Col, Alert, Button, Form } from 'react-bootstrap';
import { Formik } from 'formik';
import Api from '../utils/api';
import { Link } from 'react-router-dom';


const container = {
	backgroundColor: '#FFFFFF', padding: '2rem', marginTop: '4rem',
	boxShadow: '4px 4px 4px lightgray', borderRadius: '6px', width: '40%'
}

const coluna = {
	margin: '0.5rem'
}

const botao = {
	backgroundColor: '#5E3797', border: '0', width: '70%'
}



export default function Login() {

	const history = useHistory();

	const [cpf, setCPF] = useState("");
	const [password, setPassword] = useState("");

	const [error, setError] = useState("");

	function handleSubmit() {
		Api.post("/user/auth", { cpf, password })

			.then((body) => {
				console.log(body.data)
				localStorage.setItem('accountId', body.data.accountId);
				localStorage.setItem('cpf', body.data.cpf);
				history.replace("/home"); //Redireciona para tela de Home
			}).catch((err) => {
				setError("Erro: " + err);
				console.log("Erro: " + err);
			});

	}

	return (
		<Formik
			initialValues={{ cpf: '', password: '' }}
			onSubmit={(values) => {
				handleSubmit();
			}}
		>
			{({
				handleSubmit,
				handleChange,
				values,
				touched,
				errors,
			}) => (

				<Form onSubmit={handleSubmit} onChange={handleChange}>
					<Container style={container}>
						<Row style={{ padding: '1rem' }}>
							<h1 className="display-6" style={{ textAlign: 'center' }}> Login </h1>
							{error ? (
								<Alert size="sm" variant="danger">{error}</Alert>
							) : null}
						</Row>

						<Row>
							<Col md style={coluna}>
								<Form.Label>CPF</Form.Label>
								<Form.Control
									type="text"
									placeholder="CPF"
									name="cpf"
									value={values.cpf}
									onChange={(e) => setCPF(e.target.value)}
								/>
							</Col>
						</Row>
						<Row style={{ paddingBottom: '1rem' }}>
							<Col md style={coluna}>
								<Form.Label>Senha</Form.Label>
								<Form.Control
									type="password"
									placeholder="Senha"
									name="password"
									value={values.password}
									onChange={(e) => setPassword(e.target.value)}
								/>
							</Col>
						</Row>
						<Col style={{ textAlign: 'center', }}>
							<Button size="lg" style={botao} type="submit"> Entrar </Button>
						</Col>
						<Col style={{ textAlign: 'center', marginTop: '1rem' }}>
                            <p> Você ainda não tem uma conta? <Link to="/"> Cadastro </Link></p>
                        </Col>
					</Container>
				</Form>
			)}
		</ Formik>
	);
}