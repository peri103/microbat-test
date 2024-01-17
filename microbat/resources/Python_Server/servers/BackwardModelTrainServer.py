from RLModelTrainServer import RLModelTrainServer
from Models.Trainer import Trainer
from servers.FeedbackFeature import FeedbackVector
from utils.Log import printMsg, genMsg
from gensim.models.fasttext import load_facebook_vectors

import torch
import yaml

class BackwardModelTrainServer(RLModelTrainServer):

    CONDITION_RESULT_NAME = "ConditionResult_"

    def __init__(self, config):
        super().__init__(config)
        self.trainer = Trainer(config)
        self.work_embedding_path = self.config["encoder.work_embedding_path"]
        print(f"Loading word embeddings from {self.work_embedding_path}")
        self.word_embeddings = load_facebook_vectors(self.work_embedding_path)

    def func(self, sock):
        printMsg("-------------------------------------------", BackwardModelTrainServer)
        mode = self.recvMsg(sock)
        if mode == RLModelTrainServer.INFERENCE_MODE:
            printMsg("Inference mode", BackwardModelTrainServer)
            printMsg("Recieve feedbacks ...", BackwardModelTrainServer)
            feedback_features = self.recieve_all_feedbacks(sock)
            while self.should_continoue(sock):
                node_order = self.recieve_node_order(sock)
                node_vector = self.recieve_node_vector(sock)
                var_vector = self.recieve_variable_vector(sock)
                var_name = self.recieve_variable_name(sock)

                predictions = []

                max_step_sim = -1.0
                max_var_sim = -1.0
                for feedback_feature in feedback_features:
                    node_sim = self.cal_node_sim(node_vector, feedback_feature.node_vector)
                    var_sim = self.cal_var_sim(var_vector, var_name, feedback_feature.variable_vector, feedback_feature.variable_name)
                    input_feature = self.list_to_tensor([node_sim, var_sim])
                    prediction = self.trainer.predict(input_feature)
                    predictions.append(prediction)
                    self.trainer.save_to_cache(node_order, input_feature, prediction, 0)
                    
                    max_step_sim = max(node_sim.item(), max_step_sim)
                    max_var_sim = max(var_sim.item(), max_var_sim)

                predictions = [str(prediction.item()) for prediction in predictions]
                # printMsg(f"Node: {node_order} \t max sim : {max_step_sim}, {max_var_sim} -> {predictions}", BackwardModelTrainServer)
                assert len(predictions) != 0, genMsg("prob is an emtpy list", BackwardModelTrainServer)
                
                self.send_predictions(sock, ",".join(predictions))
                # alpha = self.cal_alpha(node_vector, feedback_vectors)
                # printMsg(f"Node: {node_order} \t backward alpha: {alpha.item()}", BackwardModelTrainServer)
                # self.send_alpha(sock, alpha.item())
            printMsg("Finish propagation ...", BackwardModelTrainServer)
        elif mode == RLModelTrainServer.REWARD_MODE:
            printMsg("Reward model", BackwardModelTrainServer)
            rewardList = []
            while self.should_continoue(sock):
                node_order = self.recieve_node_order(sock)
                reward = self.recieve_reward(sock)
                rewardList.append((node_order, reward))
                # printMsg(f"Received reward {node_order} : {reward}", BackwardModelTrainServer)
            self.trainer.update_cache_reward(rewardList)
            self.trainer.save_cache_to_memory()
            self.trainer.clear_cache()
            self.trainer.optimize_model()
            printMsg("Finish reward ...", BackwardModelTrainServer)
        else:
            raise NotImplementedError()
        
        # feedback_features = self.recieve_all_feedbacks(sock)
        # while self.should_continoue(sock):
        #     node_order = self.recieve_node_order(sock)
        #     node_vector = self.recieve_node_vector(sock)
        #     var_vector = self.recieve_variable_vector(sock)
        #     var_name = self.recieve_variable_name(sock)
        #     var_name_vector = self.gen_word_embedding(var_name)

        #     related_feedbacks = []
        #     for feedback_feature in feedback_features:
        #         feedback_node_vector = feedback_feature.node_vector
        #         if self.cosine_sim(feedback_node_vector, node_vector) > self.feedback_sim:
        #             related_feedbacks.append(feedback_feature)

        #     if len(related_feedbacks) == 0:
        #         input_feature = self.list_to_tensor([node_vector, var_vector, torch.tensor([0.0]) , torch.Tensor(FeedbackVector.DIMENSION)])
        #         prob = self.trainer.predict_prob(input_feature)
        #         reward = torch.tensor([0]).to(self.device)
        #     else:
        #         probs = []
        #         rewards = []
        #         for related_feedback in related_feedbacks:
        #             if self.is_condition_result(var_name):
        #                 sim = torch.tensor([1.0]) if related_feedback.feedback_vector.is_wrong_path_feedback() else torch.tensor([0.0])
        #             else:
        #                 sim = torch.tensor([0.0]) if related_feedback.feedback_vector.is_wrong_path_feedback() else self.cosine_sim(var_name_vector, self.gen_word_embedding(related_feedback.variable_name))
        #             input_feature = self.list_to_tensor([node_vector, var_vector, sim, related_feedback.feedback_vector.vector])
        #             prob = self.trainer.predict_prob(input_feature)
        #             probs.append(prob)
        #             rewards.append(self.cal_reward(prob, related_feedback, var_vector, var_name, var_name_vector))
        #         prob = sum(probs) / len(probs)
        #         reward = sum(rewards) / len(rewards)

        #     printMsg(f"Node: {node_order} \t backward factor: {prob.item()} \trelated feedback count: {len(related_feedbacks)} \treward: {reward.item()}", BackwardModelTrainServer)
        #     self.send_prob(sock, prob.item())
        #     self.trainer.save_to_cache(input_feature, prob, reward)

        # reward = self.recieve_reward(sock)
        # reward = torch.tensor([reward]).to(self.device)
        # printMsg(f"path reward: {reward.item()}", BackwardModelTrainServer)
        # self.trainer.update_cache_reward(reward)
        # self.trainer.save_cache_to_memory()
        # self.trainer.clear_cache()
        # self.trainer.optimize_model()
        # printMsg("Finish propagation ...", BackwardModelTrainServer)
    
    def gen_word_embedding(self, word):
        return torch.tensor(self.word_embeddings[word]).to(self.device)

    def cal_var_sim(self, var_type_1, var_name_1, var_type_2, var_name_2):
        type_sim = self.cosine_sim(var_type_1, var_type_2)
        var_name_vector_1 = self.gen_word_embedding(var_name_1)
        var_name_vector_2 = self.gen_word_embedding(var_name_2)
        name_sim = self.cosine_sim(var_name_vector_1, var_name_vector_2)
        return type_sim * name_sim
    
    def cal_reward(self, predict, feedback_feature, ref_var_type, ref_var_name, ref_var_name_vector):
        feedback_vector = feedback_feature.feedback_vector
        target_var_type = feedback_feature.variable_vector
        target_var_name = feedback_feature.variable_name
        target_var_name_vector = self.gen_word_embedding(target_var_name)
        if feedback_vector.is_correct_feedback():
            expected = 0
        elif feedback_vector.is_wrong_path_feedback():
            if self.is_condition_result(ref_var_name):
                expected = 1
            else:
                expected = 0.5
        elif feedback_vector.is_wrong_var_feedback():
            if self.is_condition_result(ref_var_name):
                expected = 0
            elif self.is_same_var(ref_var_type, ref_var_name_vector, target_var_type, target_var_name_vector):
                expected = 1
            else:
                expected = 0
        return 1 - (expected-predict)**2

    def is_same_var(self, ref_var_type, ref_var_name_vector, target_var_type, target_var_name_vector):
        return self.cosine_sim(ref_var_name_vector, target_var_name_vector) > self.var_name_sim and self.cosine_sim(ref_var_type, target_var_type) > self.var_type_sim
    
    def is_condition_result(self, var_name):
        return var_name.startswith(BackwardModelTrainServer.CONDITION_RESULT_NAME)

if __name__ == "__main__":
    config_path = "C:\\Users\\david\\git\\microbat\\microbat\\Python_Server\\servers\\configs\\backward_server_config.yaml"
    with open(config_path, "r") as yaml_file:
        config = yaml.safe_load(yaml_file)
    server = BackwardModelTrainServer(config)
    print("Server start ...")
    server.start()
    print("Server end")