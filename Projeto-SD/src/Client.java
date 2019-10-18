public class Client {
    String typeOfClient;
    String username;
    UI userUI;

    Client() {
        this.typeOfClient = "admin";
        userUI = new UI(this);
        userUI.mainMenu();
    }

    public void login() {
        // Chamar metodo do server RMI para verificar se user ja existe.
        // Se sim, entao meter outra vez a página login do UI userUI.login()
        // Se nao, dizer "Registo bem sucedido" e ir para o userUI.mainMenu()
        // Change type of user

        userUI.mainMenu();
    }

    public void register() {
        // Chamar metodo do server RMI para verificar se user ja existe.
        // Se sim, entao meter outra vez a página registo do UI userUI.register()
        // Se nao, dizer "Registo bem sucedido" e ir para o userUI.mainMenu()
        // Change type of user

        userUI.mainMenu();
    }

    public void logout(boolean result) {
        if (result == true) {
            this.typeOfClient = "anonymous";
        }
        System.out.println("\nLogout successful. You are now an anonymous user.\n");
        userUI.mainMenu();
    }

    public void shutdown() {
        // Desligar conexao
        System.out.println("\nShutdown complete.\nHope to see you again soon! :)");
        System.exit(1);
    }

    public void search(String[] words) {
        // Chamar metodo do server RMI para enviar termos de procura
        // Listar termos obtidos
        // Voltar para o search menu

        userUI.search();
    }

    public void searchHistory() {
        // Get searches from database and send them to userUI.searchHistory to be
        // printed
        // Call RMI Server method to query all the searches
    }

    public void indexNewURL(String url) {
        // Send URL from RMI server to Multicast Server to be indexed
        // return to indexNewURL menu to index more
        userUI.indexNewURL();
    }

    public void realTimeStatistics() {
        // Ask to the multicast server?
    }

    public void getUsers() {
        // Get users para conceder admin a um
        // Pedir ao RMI Server os clientes todos
    }

    public static void main(String[] args) {
        Client client = new Client();
    }

}