package tn.spring.pispring.ServiceIMP;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import tn.spring.pispring.Entities.Ticket;
import tn.spring.pispring.Entities.User;
import tn.spring.pispring.repo.TicketRepository;
import tn.spring.pispring.repo.UserRepository;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Service
public class InsightVMService {

    private static final Logger LOGGER = Logger.getLogger(InsightVMService.class.getName());
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private TicketRepository ticketRepository;
    private UserRepository userRepository;

    @Value("${insightvm.api.url}")
    private String apiUrl;

    @Value("${insightvm.api.user}")
    private String apiUser;

    @Value("${insightvm.api.password}")
    private String apiPassword;

    @Autowired
    public InsightVMService(RestTemplate restTemplate, TicketRepository ticketRepository, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 60000) // Exécution toutes les minutes (60000 ms = 1 minute)
    public void scheduledScanAndSave() {
        getSiteInformation();
        generateTicketsForAllSites();
        // Exécute la méthode pour générer et enregistrer les tickets
    }


    private String getSites() {
        String url = apiUrl + "/sites";
        HttpHeaders headers = createHeaders(apiUser, apiPassword);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        System.out.println("Response from getSites: " + responseBody); // For debugging

        return responseBody;
    }

    private String getAssetsForSite(String siteId) {
        String url = apiUrl + "/sites/" + siteId + "/assets";
        HttpHeaders headers = createHeaders(apiUser, apiPassword);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        System.out.println("Response from getAssetsForSite: " + responseBody); // For debugging

        return responseBody;
    }

    public ResponseEntity<String> generateTicketsForSite(String siteId) {
        // Step 1: Fetch asset IDs for the site
        List<String> assetIds = getAssetIdsForSite(siteId);

        // Step 2: Iterate over each asset ID to fetch vulnerabilities and create tickets
        for (String assetId : assetIds) {
            // Fetch vulnerabilities for the asset
            String vulnerabilitiesJson = getVulnerabilitiesForAsset(assetId);

            // Create tickets from the vulnerabilities JSON
            //generateTicketsFromJson(vulnerabilitiesJson);
        }

        return ResponseEntity.ok("Tickets generated successfully for site ID: " + siteId);
    }

    // Helper method to get vulnerabilities for a specific asset
    private String getVulnerabilitiesForAsset(String assetId) {
        String url = apiUrl + "/assets/" + assetId + "/vulnerabilities";
        HttpHeaders headers = createHeaders(apiUser, apiPassword);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        System.out.println("Response from getVulnerabilitiesForAsset: " + responseBody); // For debugging

        return responseBody;
    }

    // Update the generateTicketsFromJson method to handle vulnerabilities for a specific asset

  /*  public List<Ticket> generateTicketsFromJson(String json) {
        List<Ticket> tickets = new ArrayList<>();

        try {
            JsonNode rootNode = mapper.readTree(json);
            JsonNode resourcesNode = rootNode.path("resources");

            if (resourcesNode.isArray()) {
                for (JsonNode node : resourcesNode) {
                    String ip = node.path("address").asText(null);
                    if (ip == null || ip.isEmpty()) {
                        continue;
                    }

                    String hostName = node.path("osfingerprint").path("systemName").asText(null);
                    String severity = determineSeverity(node.path("vulnerabilities"));

                    for (JsonNode vuln : node.path("vulnerabilities")) {
                        String vulnId = vuln.path("id").asText();
                        String description = vuln.path("vector").asText();
                        String vector = getVulnerabilityDetail(vulnId).path("vector").asText();
                        String remediation = getRemediationDetails(node.path("text").asText(), vulnId);

                        Optional<Ticket> existingTicketOptional = Optional.ofNullable(ticketRepository.findByAssetId(ip));
                        Ticket ticket;
                        if (existingTicketOptional.isPresent()) {
                            ticket = existingTicketOptional.get();
                            updateTicket(ticket, severity, description, vector, remediation);
                        } else {
                            ticket = new Ticket(ip, severity, description, remediation);
                        }

                        tickets.add(ticket);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }

        return tickets;
    }*/

    private List<String> getVulnerabilityIdsForAsset(String assetId) {
        String url = String.format("%s/assets/%s/vulnerabilities", apiUrl, assetId);
        HttpHeaders headers = createHeaders(apiUser, apiPassword);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String responseBody = response.getBody();
            List<String> vulnerabilityIds = new ArrayList<>();
            JsonNode rootNode = mapper.readTree(responseBody);
            JsonNode resourcesNode = rootNode.path("resources");

            if (resourcesNode.isArray()) {
                for (JsonNode vulnNode : resourcesNode) {
                    String vulnId = vulnNode.path("id").asText();
                    vulnerabilityIds.add(vulnId);
                }
            }
            return vulnerabilityIds;
        } catch (Exception e) {
            System.err.println("Error fetching vulnerability IDs for asset: " + e.getMessage());
            return new ArrayList<>();
        }
    }




    private Ticket createTicketWithDetails(String assetId, String vulnerabilityId, String siteName) {
        try {
            // Récupération des détails de la vulnérabilité
            String vulnerabilityJson = getVulnerabilityDetails(vulnerabilityId);
            if (vulnerabilityJson == null) {
                LOGGER.severe("Aucun détail de vulnérabilité trouvé pour l'ID : " + vulnerabilityId);
                return null;
            }

            JsonNode vulnerabilityNode = mapper.readTree(vulnerabilityJson);
            String ip = vulnerabilityNode.path("id").asText();
            String description = vulnerabilityNode.path("title").asText();
            String severity = vulnerabilityNode.path("severity").asText();

            // Récupération des détails de la remédiation
            String remediationJson = getRemediationDetails(assetId, vulnerabilityId);
            if (remediationJson == null) {
                LOGGER.severe("Aucun détail de remédiation trouvé pour l'asset ID : " + assetId + " et la vulnérabilité ID : " + vulnerabilityId);
                return null;
            }

            JsonNode remediationNode = mapper.readTree(remediationJson);
            JsonNode resourcesNode = remediationNode.path("resources").get(0);
            JsonNode stepsNode = resourcesNode.path("steps");
            String remediationHtml = stepsNode.path("html").asText();

            String remediationText = Jsoup.parse(remediationHtml).text();

            Optional<Ticket> existingTicketOpt = Optional.ofNullable(ticketRepository.findByAssetId(assetId));
            Ticket ticket;

            if (existingTicketOpt.isPresent()) {
                ticket = existingTicketOpt.get();
                updateTicket(ticket, severity, description, remediationText);
            } else {
                Ticket newTicket = new Ticket(ip, severity, description, remediationText);
                newTicket.setSiteName(siteName); // Assigner le siteName au ticket
                ticketRepository.save(newTicket);
                return newTicket;
            }

        } catch (IOException e) {
            LOGGER.severe("Erreur lors de la création du ticket avec les détails : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la création du ticket avec les détails", e);
        }
        return null;
    }

    public List<Ticket> createTicketsForSite(String siteId) {
        List<Ticket> tickets = new ArrayList<>();
        List<String> assetIds = getAssetIdsForSite(siteId);

        // Vérifier si le site a déjà été traité
        if (processedSiteIds.contains(siteId)) {
            // Si oui, ne pas recréer les tickets pour ce site
            System.out.println("Tickets for siteId " + siteId + " have already been created.");
            return null;
        }

        // Récupérer la liste des sites à partir de l'API
        String sitesJson = getSites(); // Méthode qui récupère les données de l'API
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(sitesJson);
            JsonNode sitesNode = rootNode.path("resources");

            String siteName = null;

            // Trouver le nom du site basé sur l'ID
            if (sitesNode.isArray()) {
                for (JsonNode siteNode : sitesNode) {
                    String currentSiteId = siteNode.path("id").asText();
                    if (siteId.equals(currentSiteId)) {
                        siteName = siteNode.path("name").asText(); // Récupérer le nom du site
                        break;
                    }
                }
            }

            // Vérifier si le nom du site a été trouvé
            if (siteName == null) {
                throw new RuntimeException("Site name not found for siteId: " + siteId);
            }

            // Vérifier si des tickets existent déjà pour ce site
            List<Ticket> existingTickets = ticketRepository.findBySiteName(siteName);
            if (!existingTickets.isEmpty()) {
                System.out.println("Tickets already exist for siteName " + siteName);
                return null; // Si des tickets existent déjà, ne pas en créer de nouveaux
            }

            // Créer les tickets pour chaque asset et chaque vulnérabilité
            for (String assetId : assetIds) {
                List<String> vulnerabilityIds = getVulnerabilityIdsForAsset(assetId);
                for (String vulnerabilityId : vulnerabilityIds) {
                    Ticket ticket = createTicketWithDetails(assetId, vulnerabilityId,siteName);
                    if (ticket != null) {
                        ticket.setSiteName(siteName); // Affecter le nom du site au ticket
                        ticket.addStatusChange(ticket.getStatus());
                        ticketRepository.save(ticket); // Sauvegarder le ticket
                        tickets.add(ticket);
                    }
                }
            }

            // Ajouter l'ID du site au Set des sites traités pour éviter la duplication
            processedSiteIds.add(siteId);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error processing site information", e);
        }

        return tickets;
    }


    private String getSiteNameById(String siteId) {
        String sitesJson = getSites(); // Récupérer la liste des sites
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(sitesJson);
            JsonNode sitesNode = rootNode.path("resources");

            if (sitesNode.isArray()) {
                for (JsonNode siteNode : sitesNode) {
                    String currentSiteId = siteNode.path("id").asText();
                    if (currentSiteId.equals(siteId)) {
                        return siteNode.path("name").asText(); // Retourne le nom du site correspondant
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Retourne null si aucun site n'est trouvé avec cet ID
    }


    private Set<String> processedSiteIds = new HashSet<>();
    public List<Ticket> generateTicketsForAllSites() {
        String sitesJson = getSites(); // Récupérer la liste des sites
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(sitesJson);
            JsonNode sitesNode = rootNode.path("resources");

            if (sitesNode.isArray()) {
                for (JsonNode siteNode : sitesNode) {
                    String siteId = siteNode.path("id").asText();

                    // Vérifier si le site a déjà été traité
                    if (!processedSiteIds.contains(siteId)) {
                        // Créer des tickets pour le site
                        createTicketsForSite(siteId);

                        // Ajouter l'ID du site au Set des sites traités
                        processedSiteIds.add(siteId);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void updateTicket(Ticket ticket, String severity, String description,  String remediation) {
        if (!ticket.getSeverity().equals(severity) || !ticket.getDescription().equals(description)) {
            ticket.setSeverity(severity);
            ticket.setDescription(description );
            ticket.setRemediation(remediation);

            ticketRepository.save(ticket);
        }
    }


    private JsonNode getVulnerabilityDetail(String vulnId) {
        String vulnDetailsJson = getVulnerabilityDetails(vulnId);
        try {
            return mapper.readTree(vulnDetailsJson);
        } catch (IOException e) {
            System.err.println("Error parsing vulnerability details: " + e.getMessage());
            return null;
        }
    }


    private String determineSeverity(JsonNode vulnNode) {
        int critical = vulnNode.path("critical").asInt();
        int severe = vulnNode.path("severe").asInt();
        int moderate = vulnNode.path("moderate").asInt();

        if (critical > 0) {
            return "Critical";
        } else if (severe > 0) {
            return "Severe";
        } else if (moderate > 0) {
            return "Moderate";
        } else {
            return "None";
        }
    }



    private void createNewTicket(String ip, String severity, String description, String remediation) {
        Ticket newTicket = new Ticket(ip, severity, remediation, description );
        newTicket.addStatusChange(newTicket.getStatus());
        ticketRepository.save(newTicket);
    }


    public String getRemediationDetails(String assetId, String vulnId) {
        String url = String.format("%s/assets/%s/vulnerabilities/%s/solution", apiUrl, assetId, vulnId);
        HttpHeaders headers = createHeaders(apiUser, apiPassword);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody(); // Return the remediation details
            } else {
                System.err.println("Failed to fetch remediation details: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error fetching remediation details: " + e.getMessage());
        }
        return null;
    }
    public String getRemediationDetailsForSite(String siteId, String vulnId) {
        // Récupère les IDs des assets pour un site donné
        List<String> assetIds = getAssetIdsForSite(siteId);

        // Collecte des détails de remédiation pour chaque asset et vulnérabilité
        StringBuilder remediationDetails = new StringBuilder();

        for (String assetId : assetIds) {
            String remediation = getRemediationDetails(assetId, vulnId);
            if (remediation != null) {
                remediationDetails.append("Asset ID: ").append(assetId).append("\n")
                        .append("Remediation: ").append(remediation).append("\n\n");
            }
        }

        return remediationDetails.toString();
    }

    public String getVulnerabilityDetails(String vulnId) {
        // URL de l'API pour récupérer les détails de la vulnérabilité par ID
        String url = String.format("%s/vulnerabilities/%s", apiUrl, vulnId);
        HttpHeaders headers = createHeaders(apiUser, apiPassword);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody(); // Return the details of the vulnerability
            } else {
                System.err.println("Failed to fetch vulnerability details: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error fetching vulnerability details: " + e.getMessage());
        }

        return null; // Return null if there was an error
    }
    public List<String> getVulnerabilityIds() {
        String url = apiUrl + "/vulnerabilities";
        HttpHeaders headers = createHeaders(apiUser, apiPassword);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        System.out.println("Response from getVulnerabilityIds: " + responseBody); // For debugging

        List<String> vulnIds = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resourcesNode = rootNode.path("resources");

            if (resourcesNode.isArray()) {
                for (JsonNode vulnNode : resourcesNode) {
                    String vulnId = vulnNode.path("id").asText();
                    vulnIds.add(vulnId);
                }
            }
        } catch (IOException e) {
            System.err.println("Error parsing vulnerability IDs: " + e.getMessage());
        }

        return vulnIds;
    }




    public List<String> getAssetIdsForSite(String siteId) {
        String url = apiUrl + "/sites/" + siteId + "/assets";
        HttpHeaders headers = createHeaders(apiUser, apiPassword);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        System.out.println("Response from getAssetsForSite: " + responseBody); // For debugging

        List<String> assetIds = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode resourcesNode = rootNode.path("resources");

            if (resourcesNode.isArray()) {
                for (JsonNode assetNode : resourcesNode) {
                    String assetId = assetNode.path("id").asText();
                    assetIds.add(assetId);
                }
            }
        } catch (IOException e) {
            System.err.println("Error parsing asset IDs: " + e.getMessage());
        }

        return assetIds;
    }
    public List<Map<String, Object>> getSiteInformation() {
        List<Map<String, Object>> siteInfos = new ArrayList<>();
        String sitesJson = getSites();

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(sitesJson);
            JsonNode resourcesNode = rootNode.path("resources");

            if (resourcesNode.isArray()) {
                for (JsonNode siteNode : resourcesNode) {
                    Map<String, Object> siteInfo = new HashMap<>();
                    siteInfo.put("name", siteNode.path("name").asText(null));
                    siteInfo.put("assets", siteNode.path("assets").asInt());
                    siteInfo.put("description", siteNode.path("description").asText(null));
                    siteInfo.put("importance", siteNode.path("importance").asText(null));
                    siteInfo.put("lastScanTime", siteNode.path("lastScanTime").asText(null));

                    siteInfos.add(siteInfo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Log and handle the exception appropriately
        }

        return siteInfos;
    }




    private HttpHeaders createHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        return headers;
    }


    public Ticket assignTicketToUser(Long ticketId, Long userId) { Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found")); User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")); ticket.setUser(user); return ticketRepository.save(ticket); }
  /*  private HttpHeaders createHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        return headers;
    }*/
    public List<Ticket> getTicketsByUserId(Long userId) {
        User user = userRepository.findUserById(userId);

        return ticketRepository.findTicketByUser(user);
    }
    // Updated Method to Retrieve Tickets for a Site by Asset ID
    public List<Ticket> getTicketsBySiteId(String siteId) {
        // Fetch assets for the site
        String assetsJson = getAssetsForSite(siteId);
        ObjectMapper mapper = new ObjectMapper();
        List<String> assetIds = new ArrayList<>();

        try {
            JsonNode rootNode = mapper.readTree(assetsJson);
            JsonNode resourcesNode = rootNode.path("resources");

            if (resourcesNode.isArray()) {
                for (JsonNode resource : resourcesNode) {
                    JsonNode addressesNode = resource.path("addresses");
                    if (addressesNode.isArray() && !addressesNode.isEmpty()) {
                        JsonNode addressNode = addressesNode.get(0); // Assuming there is at least one address
                        String ip = addressNode.path("ip").asText(null); // Default to null if not present
                        if (ip != null && !ip.isEmpty()) {
                            assetIds.add(ip);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fetch tickets based on asset IDs
        return ticketRepository.findTicketsByAssetIdIn(assetIds);
    }
    public String getSiteIdByName(String siteName) {
        String sitesJson = getSites(); // Récupère la liste des sites
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(sitesJson);
            JsonNode sitesNode = rootNode.path("resources");

            if (sitesNode.isArray()) {
                for (JsonNode siteNode : sitesNode) {
                    if (siteNode.path("name").asText().equals(siteName)) {
                        return siteNode.path("id").asText(); // Retourne l'ID du site si le nom correspond
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Retourne null si le site avec ce nom n'a pas été trouvé
    }
    public List<Ticket> getTicketsBySiteName(String siteName) {
        String siteId = getSiteIdByName(siteName); // Obtient l'ID du site par son nom
        if (siteId == null) {
            throw new RuntimeException("Site not found with name: " + siteName);
        }
        // Utilise l'ID du site pour récupérer les assets et les tickets
        return getTicketsBySiteId(siteId);
    }
    public Ticket getTicketDetailsById(Long ticketId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        return ticketOptional.orElse(null);
    }

   /* public Map<String, Object> getDetailedTicketInformationByIp(String ipAddress) {
        String sitesJson = getSites(); // Fetch the list of sites
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode rootNode = mapper.readTree(sitesJson);
            JsonNode sitesNode = rootNode.path("resources");

            if (sitesNode.isArray()) {
                for (JsonNode siteNode : sitesNode) {
                    String siteId = siteNode.path("id").asText();
                    String siteName = siteNode.path("name").asText();

                    String assetsJson = getAssetsForSite(siteId);
                    JsonNode assetsNode = mapper.readTree(assetsJson).path("resources");

                    if (assetsNode.isArray()) {
                        for (JsonNode assetNode : assetsNode) {
                            JsonNode addressesNode = assetNode.path("addresses");
                            if (addressesNode.isArray() && !addressesNode.isEmpty()) {
                                String ip = addressesNode.get(0).path("ip").asText(null);
                                if (ipAddress.equals(ip)) {
                                    // Extract ticket information
                                    Map<String, Object> ticketInfo = new HashMap<>();
                                    String mac = assetNode.path("mac").asText(null);
                                    String riskScore = assetNode.path("riskScore").asText("N/A");
                                    String severity = "None";

                                    JsonNode vulnerabilitiesNode = assetNode.path("vulnerabilities");
                                    int critical = vulnerabilitiesNode.path("critical").asInt();
                                    int severe = vulnerabilitiesNode.path("severe").asInt();
                                    int moderate = vulnerabilitiesNode.path("moderate").asInt();

                                    if (critical > 0) {
                                        severity = "Critical";
                                    } else if (severe > 0) {
                                        severity = "Severe";
                                    } else if (moderate > 0) {
                                        severity = "Moderate";
                                    }

                                    String description = "Asset with IP " + ip + " has vulnerabilities ";

                                    // Extract history
                                    List<Map<String, Object>> historyList = new ArrayList<>();
                                    JsonNode historyNode = assetNode.path("history");
                                    if (historyNode.isArray()) {
                                        for (JsonNode historyEntry : historyNode) {
                                            Map<String, Object> historyMap = new HashMap<>();
                                            historyMap.put("date", historyEntry.path("date").asText(null));
                                            historyMap.put("type", historyEntry.path("type").asText(null));
                                            historyMap.put("version", historyEntry.path("version").asInt());
                                            historyList.add(historyMap);
                                        }
                                    }
                                    Ticket ticket = ticketRepository.findByAssetId(ipAddress);
                                    String status = String.valueOf(ticket.getStatus());
                                    // Populate ticket info
                                    ticketInfo.put("siteName", siteName);
                                    ticketInfo.put("severity", severity);
                                    ticketInfo.put("status", status);
                                    ticketInfo.put("ip", ip);
                                    ticketInfo.put("description", description);
                                    ticketInfo.put("riskScore", riskScore);
                                    ticketInfo.put("history", historyList);

                                    // Save or update the ticket in the database
                                    Optional<Ticket> existingTicketOptional = Optional.ofNullable(ticketRepository.findByAssetId(ip));


                                    if (existingTicketOptional.isPresent()) {
                                        ticket = existingTicketOptional.get();
                                        ticket.setSeverity(severity);
                                        ticket.setDescription(description);
                                        // Update other fields if necessary
                                    } else {
                                        ticket = new Ticket(ip, severity, description);
                                    }
                                    // Save or update ticket in the database
                                    ticketRepository.save(ticket);

                                    return ticketInfo; // Return the details of the found ticket
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Return null if the ticket is not found
    }


*/


}
